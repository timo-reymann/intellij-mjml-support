use std::ffi::CString;
use std::os::raw::c_char;
use std::mem;

#[unsafe(no_mangle)]
pub extern "C" fn free_string(ptr: *mut c_char) {
    if ptr.is_null() {
        return;
    }

    unsafe {
        drop(CString::from_raw(ptr));
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn alloc_string(len: i32) -> *const u8 {
    let mut buf = Vec::with_capacity(len as usize);
    let ptr = buf.as_mut_ptr();
    // tell Rust not to clean this up
    mem::forget(buf);
    ptr
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::ffi::CString;

    #[test]
    fn test_free_string_valid_pointer() {
        // Create a CString that we'll later free
        let test_string = "test string";
        let c_string = CString::new(test_string).unwrap();
        let raw_ptr = c_string.into_raw();

        // Call free_string - if this causes a double free or other memory issue,
        // the test will crash
        free_string(raw_ptr);
    }

    #[test]
    fn test_free_string_null_pointer() {
        // Test that passing a null pointer doesn't cause any crashes
        let null_ptr: *mut c_char = std::ptr::null_mut();
        free_string(null_ptr);
        // If we reached here, it means the function handled null pointer correctly
    }

    #[test]
    fn test_free_string_multiple_calls() {
        // Create multiple strings and free them to ensure no memory leaks
        for _ in 0..100 {
            let test_string = "test string";
            let c_string = CString::new(test_string).unwrap();
            let raw_ptr = c_string.into_raw();
            free_string(raw_ptr);
        }
    }

    #[test]
    fn test_alloc_string_zero_length() {
        let ptr = alloc_string(0);
        assert!(!ptr.is_null());
        // We don't free the pointer here since it's just a capacity test
    }

    #[test]
    fn test_alloc_string_positive_length() {
        let len = 10;
        let ptr = alloc_string(len);
        assert!(!ptr.is_null());

        // Verify we can write to the allocated memory
        unsafe {
            for i in 0..len {
                *((ptr as *mut u8).add(i as usize)) = i as u8;
            }
        }
    }

    #[test]
    fn test_alloc_string_large_allocation() {
        let len = 1000000; // 1MB
        let ptr = alloc_string(len);
        assert!(!ptr.is_null());
    }
}
