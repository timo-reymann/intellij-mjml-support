use mrml::prelude::parser::loader::{IncludeLoader, IncludeLoaderError};
use std::fs;
use std::io::ErrorKind;
use std::path::PathBuf;
use std::sync::Arc;

#[derive(Debug, Default)]
pub struct LocalWithoutPrefixIncludeLoader {
    root: PathBuf,
}

impl LocalWithoutPrefixIncludeLoader {
    pub fn new(root: PathBuf) -> Self {
        Self { root }
    }
}

impl IncludeLoader for LocalWithoutPrefixIncludeLoader {
    fn resolve(&self, url: &str) -> Result<String, IncludeLoaderError> {
        let path = self.root.join(url);
        fs::read_to_string(path).map_err(|err| {
            IncludeLoaderError::new(url, ErrorKind::InvalidData)
                .with_message("unable to load the template file")
                .with_cause(Arc::new(err))
        })
    }
}


#[cfg(test)]
mod tests {
    use super::*;
    use std::fs::File;
    use std::io::Write;
    use tempfile::TempDir;

    fn create_temp_file(dir: &TempDir, filename: &str, content: &str) -> PathBuf {
        let file_path = dir.path().join(filename);
        let mut file = File::create(&file_path).unwrap();
        file.write_all(content.as_bytes()).unwrap();
        file_path
    }

    #[test]
    fn test_successful_file_load() {
        let temp_dir = TempDir::new().unwrap();
        let test_content = "Hello, World!";
        let filename = "test.txt";
        create_temp_file(&temp_dir, filename, test_content);

        let loader = LocalWithoutPrefixIncludeLoader::new(temp_dir.path().to_path_buf());
        let result = loader.resolve(filename);

        assert!(result.is_ok());
        assert_eq!(result.unwrap(), test_content);
    }


    #[test]
    fn test_nested_path() {
        let temp_dir = TempDir::new().unwrap();
        std::fs::create_dir(temp_dir.path().join("subdir")).unwrap();

        let test_content = "Nested content";
        let filename = "subdir/nested.txt";
        create_temp_file(&temp_dir, filename, test_content);

        let loader = LocalWithoutPrefixIncludeLoader::new(temp_dir.path().to_path_buf());
        let result = loader.resolve(filename);

        assert!(result.is_ok());
        assert_eq!(result.unwrap(), test_content);
    }

    #[test]
    fn test_new_creates_loader() {
        let path = PathBuf::from("/some/path");
        let loader = LocalWithoutPrefixIncludeLoader::new(path.clone());
        assert_eq!(loader.root, path);
    }

    #[test]
    fn test_default_creates_empty_path_loader() {
        let loader = LocalWithoutPrefixIncludeLoader::default();
        assert_eq!(loader.root, PathBuf::new());
    }
}