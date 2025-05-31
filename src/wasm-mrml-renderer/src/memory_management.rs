use std::ffi::CString;
use std::os::raw::c_char;

#[unsafe(no_mangle)]
pub extern "C" fn free_string(ptr: *mut c_char) {
    if ptr.is_null() {
        return;
    }

    unsafe {
        drop(CString::from_raw(ptr));
    }
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
}