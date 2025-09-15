use base::{
    LOGGER, LogLevel, Logger, SilentResultExt, Utf8CStr, cstr,
    libc::{
        O_CLOEXEC, O_RDWR, O_WRONLY, S_IFCHR, STDERR_FILENO, STDIN_FILENO, STDOUT_FILENO, SYS_dup3,
        makedev, mknod, syscall,
    },
    raw_cstr,
};
use std::mem::ManuallyDrop;
use std::{
    fs::File,
    io::{IoSlice, Write},
    os::fd::{FromRawFd, IntoRawFd, RawFd},
};

// SAFETY: sunnyinit is single threaded
static mut KMSG: RawFd = -1;

// 日志相关配置
// 核心目的是初始化系统日志输出，将日志定向到内核日志设备（/dev/kmsg）。
// 同时处理标准文件描述符（stdin/stdout/stderr）的重定向，并禁用kmsg速率限制，确保日志能正常输出到内核。
pub fn setup_klog() {
    unsafe {
        // 重定向标准文件描述符（stdin/stdout/stderr）
        // Shut down first 3 fds
        let mut fd = cstr!("/dev/null").open(O_RDWR | O_CLOEXEC).silent();  // 尝试打开 /dev/null 作为标准输入输出，若打开失败则创建临时设备 /null 并打开
        if fd.is_err() {
            mknod(raw_cstr!("/null"), S_IFCHR | 0o666, makedev(1, 3));  // 创建字符设备（1,3 对应 /dev/null）
            fd = cstr!("/null").open(O_RDWR | O_CLOEXEC).silent();
            cstr!("/null").remove().ok();  // 用完后删除临时设备
        }
        // 若成功打开，则通过 dup3 系统调用将其复制到标准输入输出（0,1,2）
        // 也就是把 stdin, stdout, stderr 都重定向到 /dev/null
        if let Ok(ref fd) = fd {
            syscall(SYS_dup3, fd, STDIN_FILENO, O_CLOEXEC);
            syscall(SYS_dup3, fd, STDOUT_FILENO, O_CLOEXEC);
            syscall(SYS_dup3, fd, STDERR_FILENO, O_CLOEXEC);
        }

        // Then open kmsg fd
        // 尝试打开 /dev/kmsg，若失败则创建临时设备 /kmsg 并打开
        let mut fd = cstr!("/dev/kmsg").open(O_WRONLY | O_CLOEXEC).silent();
        if fd.is_err() {
            mknod(raw_cstr!("/kmsg"), S_IFCHR | 0o666, makedev(1, 11));  // 创建字符设备（1，11 对应 /dev/kmsg）
            fd = cstr!("/kmsg").open(O_WRONLY | O_CLOEXEC).silent();
            cstr!("/kmsg").remove().ok();  // 用完后删除临时设备
        }
        // 将打开的 kmsg 文件描述符存储在静态变量 KMSG 中，供后续日志写入使用
        KMSG = fd.map(|fd| fd.into_raw_fd()).unwrap_or(-1);
    }

    // Disable kmsg rate limiting
    // 防止日志输出被内核速率限制，确保所有日志都能被写入
    if let Ok(mut rate) = cstr!("/proc/sys/kernel/printk_devkmsg").open(O_WRONLY | O_CLOEXEC) {
        // /proc/sys/kernel/printk_devkmsg 是控制 kmsg 速率限制的内核参数，写入 "on" 表示禁用限制。
        writeln!(rate, "on").ok();
    }
    // 定义日志写入函数，将日志消息写入内核日志设备
    fn kmsg_log_write(_: LogLevel, msg: &Utf8CStr) {
        let fd = unsafe { KMSG };
        if fd >= 0 {
            let io1 = IoSlice::new("sunnyinit: ".as_bytes());
            let io2 = IoSlice::new(msg.as_bytes());
            let mut kmsg = ManuallyDrop::new(unsafe { File::from_raw_fd(fd) });
            let _ = kmsg.write_vectored(&[io1, io2]).ok();
        }
    }

    let logger = Logger {
        write: kmsg_log_write,
        flags: 0,
    };
    // 设置全局日志器
    unsafe {
        LOGGER = logger;
    }
}
