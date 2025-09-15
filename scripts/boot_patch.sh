#!/system/bin/sh
#######################################################################################
# Sunny Boot Image Patcher
#######################################################################################
#
# Usage: boot_patch.sh <bootimage>
#
# The following environment variables can configure the installation:
# KEEPVERITY, KEEPFORCEENCRYPT, PATCHVBMETAFLAG, RECOVERYMODE, LEGACYSAR
# keep_verity, keep_force_encrypt, patch_vbmeta_flag, recovery_mode, legacy_sar
#
# This script should be placed in a directory with the following files:
#
# File name          Type      Description
#
# boot_patch.sh      script    A script to patch boot image for Sunny.
#                  (this file) The script will use files in its same
#                              directory to complete the patching process.
# util_functions.sh  script    A script which hosts all functions required
#                              for this script to work properly.
# sunnyinit         binary    The binary to replace /init.
# sunny             binary    The sunny binary.
# sunnyboot         binary    A tool to manipulate boot images.  用来处理 boot image 的工具
# init-ld            binary    The library that will be LD_PRELOAD of /init
# stub.apk           binary    The stub Sunny app to embed into ramdisk.
# chromeos           folder    This folder includes the utility and keys to sign
#                  (optional)  chromeos boot images. Only used for Pixel C.
#
#######################################################################################

############
# Functions
############

# Pure bash dirname implementation
getdir() {
  case "$1" in
    */*)
      dir=${1%/*}
      if [ -z $dir ]; then
        echo "/"
      else
        echo $dir
      fi
    ;;
    *) echo "." ;;
  esac
}

#################
# Initialization
#################

if [ -z $SOURCEDMODE ]; then
  # 如果不是被 source 的话，执行下面初始化流程
  # Switch to the location of the script file
  cd "$(getdir "${BASH_SOURCE:-$0}")"
  # Load utility functions
  . ./util_functions.sh
  # Check if 64-bit
  # 检测cpu架构，是否为 64 位系统
  api_level_arch_detect
fi

# 获取传入的boot.img路径，如果未传入
BOOTIMAGE="$1"
[ -e "$BOOTIMAGE" ] || abort "$BOOTIMAGE does not exist!"

# Dump image for MTD/NAND character device boot partitions
if [ -c "$BOOTIMAGE" ]; then
  # -c 判断是否为字符设备文件，如果是的话，
  # 使用 nanddump 命令将其内容导出到当前目录下的 boot.img 文件中
  ui_print ">>> Dumping boot image from $BOOTIMAGE"
  nanddump -f boot.img "$BOOTIMAGE"
  BOOTNAND="$BOOTIMAGE"
  BOOTIMAGE=boot.img
fi

# Flags
# 设置默认值，如果环境变量未设置的话
[ -z $KEEPVERITY ] && KEEPVERITY=false
[ -z $KEEPFORCEENCRYPT ] && KEEPFORCEENCRYPT=false
[ -z $PATCHVBMETAFLAG ] && PATCHVBMETAFLAG=false
[ -z $RECOVERYMODE ] && RECOVERYMODE=false
[ -z $LEGACYSAR ] && LEGACYSAR=false
export KEEPVERITY
export KEEPFORCEENCRYPT
export PATCHVBMETAFLAG

chmod -R 755 .

#########
# Unpack
#########

CHROMEOS=false

ui_print "- Unpacking boot image"
./sunnyboot unpack "$BOOTIMAGE"

case $? in
  0 ) ;;
  1 )
    abort "! Unsupported/Unknown image format"
    ;;
  2 )
    ui_print "- ChromeOS boot image detected"
    CHROMEOS=true
    ;;
  * )
    abort "! Unable to unpack boot image"
    ;;
esac

###################
# Ramdisk Restores
###################

# Test patch status and do restore
# 检测 ramdisk 状态并进行恢复
ui_print "- Checking ramdisk status"
if [ -e ramdisk.cpio ]; then
  ./sunnyboot cpio ramdisk.cpio test
  STATUS=$?
  SKIP_BACKUP=""
else
  # Stock A only legacy SAR, or some Android 13 GKIs
  STATUS=0
  SKIP_BACKUP="#"
fi
case $STATUS in
  0 )
    # Stock boot
    ui_print "- Stock boot image detected"
    SHA1=$(./sunnyboot sha1 "$BOOTIMAGE" 2>/dev/null)
    cat $BOOTIMAGE > stock_boot.img
    cp -af ramdisk.cpio ramdisk.cpio.orig 2>/dev/null
    ;;
  1 )
    # Sunny patched
    ui_print "- Sunny patched boot image detected"
    ./sunnyboot cpio ramdisk.cpio \
    "extract .backup/.sunny config.orig" \
    "restore"
    cp -af ramdisk.cpio ramdisk.cpio.orig
    rm -f stock_boot.img
    ;;
  2 )
    # Unsupported
    ui_print "! Boot image patched by unsupported programs"
    abort "! Please restore back to stock boot image"
    ;;
esac

# 如果存在 config.orig 文件，读取其中的配置
if [ -f config.orig ]; then
  # Read existing configs
  chmod 0644 config.orig
  SHA1=$(grep_prop SHA1 config.orig)
  if ! $BOOTMODE; then
    # Do not inherit config if not in recovery
    PREINITDEVICE=$(grep_prop PREINITDEVICE config.orig)
  fi
  rm config.orig
fi

##################
# Ramdisk Patches
##################

ui_print "- Patching ramdisk"

$BOOTMODE && [ -z "$PREINITDEVICE" ] && PREINITDEVICE=$(./sunny --preinit-device)

# Compress to save precious ramdisk space
./sunnyboot compress=xz sunny sunny.xz
./sunnyboot compress=xz stub.apk stub.xz
./sunnyboot compress=xz init-ld init-ld.xz

echo "KEEPVERITY=$KEEPVERITY" > config
echo "KEEPFORCEENCRYPT=$KEEPFORCEENCRYPT" >> config
echo "RECOVERYMODE=$RECOVERYMODE" >> config
if [ -n "$PREINITDEVICE" ]; then
  ui_print "- Pre-init storage partition: $PREINITDEVICE"
  echo "PREINITDEVICE=$PREINITDEVICE" >> config
fi
[ -n "$SHA1" ] && echo "SHA1=$SHA1" >> config

# 这段是利用 sunnyboot 工具对 ramdisk 进行一系列操作，核心是把 sunnyinit 文件添加为 init 文件
#"add 0750 init sunnyinit" \     # 向 ramdisk 中添加文件：将 sunnyinit（Sunny 初始化程序）添加为 init 文件，权限设为 0750（所有者可读可写可执行，同组可读可执行）
./sunnyboot cpio ramdisk.cpio \
"add 0750 init sunnyinit" \
"mkdir 0750 overlay.d" \
"mkdir 0750 overlay.d/sbin" \
"add 0644 overlay.d/sbin/sunny.xz sunny.xz" \
"add 0644 overlay.d/sbin/stub.xz stub.xz" \
"add 0644 overlay.d/sbin/init-ld.xz init-ld.xz" \
"patch" \
"$SKIP_BACKUP backup ramdisk.cpio.orig" \
"mkdir 000 .backup" \
"add 000 .backup/.sunny config" \
|| abort "! Unable to patch ramdisk"

rm -f ramdisk.cpio.orig config *.xz

#################
# Binary Patches
#################
# 这部分是对内核和 dtb 进行一些补丁操作，特别是针对三星设备的一些补丁操作

for dt in dtb kernel_dtb extra; do
  if [ -f $dt ]; then
    if ! ./sunnyboot dtb $dt test; then
      ui_print "! Boot image $dt was patched by old (unsupported) Sunny"
      abort "! Please try again with *unpatched* boot image"
    fi
    if ./sunnyboot dtb $dt patch; then
      ui_print "- Patch fstab in boot image $dt"
    fi
  fi
done

if [ -f kernel ]; then
  PATCHEDKERNEL=false
  # Remove Samsung RKP
  ./sunnyboot hexpatch kernel \
  49010054011440B93FA00F71E9000054010840B93FA00F7189000054001840B91FA00F7188010054 \
  A1020054011440B93FA00F7140020054010840B93FA00F71E0010054001840B91FA00F7181010054 \
  && PATCHEDKERNEL=true

  # Remove Samsung defex
  # Before: [mov w2, #-221]   (-__NR_execve)
  # After:  [mov w2, #-32768]
  ./sunnyboot hexpatch kernel 821B8012 E2FF8F12 && PATCHEDKERNEL=true

  # Disable Samsung PROCA
  # proca_config -> proca_sunny
  ./sunnyboot hexpatch kernel \
  70726F63615F636F6E66696700 \
  70726F63615F6D616769736B00 \
  && PATCHEDKERNEL=true

  # Force kernel to load rootfs for legacy SAR devices
  # skip_initramfs -> want_initramfs
  $LEGACYSAR && ./sunnyboot hexpatch kernel \
  736B69705F696E697472616D667300 \
  77616E745F696E697472616D667300 \
  && PATCHEDKERNEL=true

  # If the kernel doesn't need to be patched at all,
  # keep raw kernel to avoid bootloops on some weird devices
  $PATCHEDKERNEL || rm -f kernel
fi

#################
# Repack & Flash
#################
# 重新打包 boot image，并签名（如果是 ChromeOS 设备）

ui_print "- Repacking boot image"
./sunnyboot repack "$BOOTIMAGE" || abort "! Unable to repack boot image"

# Sign chromeos boot
$CHROMEOS && sign_chromeos

# Restore the original boot partition path
[ -e "$BOOTNAND" ] && BOOTIMAGE="$BOOTNAND"

# Reset any error code
true
