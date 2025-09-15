#![allow(dead_code)]
use base::const_format::concatcp;

#[path = "../../out/generated/flags.rs"]
mod flags;

// versions
pub use flags::*;
pub const SUNNY_FULL_VER: &str = concatcp!(SUNNY_VERSION, "(", SUNNY_VER_CODE, ")");

pub const APP_PACKAGE_NAME: &str = "com.fastjien.sunny";

pub const LOGFILE: &str = "/cache/sunny.log";

// data paths
pub const SECURE_DIR: &str = "/data/adb";
pub const MODULEROOT: &str = concatcp!(SECURE_DIR, "/modules");
pub const MODULEUPGRADE: &str = concatcp!(SECURE_DIR, "/modules_update");
pub const DATABIN: &str = concatcp!(SECURE_DIR, "/sunny");
pub const SUNNYDB: &str = concatcp!(SECURE_DIR, "/sunny.db");

// tmpfs paths
pub const INTERNAL_DIR: &str = ".sunny";
pub const MAIN_CONFIG: &str = concatcp!(INTERNAL_DIR, "/config");
pub const PREINITMIRR: &str = concatcp!(INTERNAL_DIR, "/preinit");
pub const MODULEMNT: &str = concatcp!(INTERNAL_DIR, "/modules");
pub const WORKERDIR: &str = concatcp!(INTERNAL_DIR, "/worker");
pub const DEVICEDIR: &str = concatcp!(INTERNAL_DIR, "/device");
pub const PREINITDEV: &str = concatcp!(DEVICEDIR, "/preinit");
pub const LOG_PIPE: &str = concatcp!(DEVICEDIR, "/log");
pub const ROOTOVL: &str = concatcp!(INTERNAL_DIR, "/rootdir");
pub const ROOTMNT: &str = concatcp!(ROOTOVL, "/.mount_list");
pub const SELINUXMOCK: &str = concatcp!(INTERNAL_DIR, "/selinux");

// Unconstrained domain the daemon and root processes run in
pub const SEPOL_PROC_DOMAIN: &str = "sunny";
pub const SUNNY_PROC_CON: &str = concatcp!("u:r:", SEPOL_PROC_DOMAIN, ":s0");
// Unconstrained file type that anyone can access
pub const SEPOL_FILE_TYPE: &str = "sunny_file";
pub const SUNNY_FILE_CON: &str = concatcp!("u:object_r:", SEPOL_FILE_TYPE, ":s0");
// Log pipe that only root and zygote can open
pub const SEPOL_LOG_TYPE: &str = "sunny_log_file";
pub const SUNNY_LOG_CON: &str = concatcp!("u:object_r:", SEPOL_LOG_TYPE, ":s0");
