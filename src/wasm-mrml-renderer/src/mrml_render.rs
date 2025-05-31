use crate::loader::LocalWithoutPrefixIncludeLoader;
use mrml::mjml::Mjml;
use mrml::prelude::parser::ParserOptions;
use mrml::prelude::parser::loader::IncludeLoader;
use mrml::prelude::parser::multi_loader::MultiIncludeLoader;
use mrml::prelude::render::RenderOptions;
use serde::{Deserialize, Serialize};
use std::ffi::{CStr, CString};
use std::os::raw::c_char;
use std::path::PathBuf;

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct RenderInputOptions {
    mjml_config_path: Option<String>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct RenderInput {
    directory: String,
    content: String,
    options: RenderInputOptions,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct RenderError {
    line: Option<u32>,
    message: Option<String>,
    tag_name: Option<String>,
    formatted_message: Option<String>,
}

#[derive(Debug, Serialize, Deserialize)]
struct RenderResult {
    html: Option<String>,
    errors: Vec<RenderError>,
}

#[unsafe(no_mangle)]
pub extern "C" fn render_mjml(input: *const c_char) -> *mut c_char {
    if input.is_null() {
        return std::ptr::null_mut();
    }

    // Convert raw C string to Rust string slice
    let c_str = unsafe { CStr::from_ptr(input) };
    let input_str = match c_str.to_str() {
        Ok(s) => s,
        Err(_) => return std::ptr::null_mut(),
    };

    let parsed_input: RenderInput = serde_json::from_str(input_str).unwrap();

    let render_result = render_mjml_result(parsed_input);
    let json_response =
        serde_json::to_string(&render_result).unwrap_or_else(|_| String::from("{}"));

    // Convert back to C string
    match CString::new(json_response) {
        Ok(c_string) => c_string.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

fn render_mjml_result(input: RenderInput) -> RenderResult {
    let template_root = PathBuf::from(input.directory.clone());
    let render_options = RenderOptions::default();
    let resolver = MultiIncludeLoader::<Box<dyn IncludeLoader>>::new().with_starts_with(
        "",
        Box::new(LocalWithoutPrefixIncludeLoader::new(template_root)),
    );

    let parser_options = ParserOptions {
        include_loader: Box::new(resolver),
    };

    let (element, error) = match mrml::parse_with_options(&input.content, &parser_options) {
        Ok(template) => (template.element, None),
        Err(err) => (Mjml::default(), Option::from(err)),
    };

    if let Some(error) = error {
        let error_message = format!("Error parsing MJML: {error}");
        return RenderResult {
            html: None,
            errors: vec![{
                RenderError {
                    line: None,
                    message: Some(error_message.clone()),
                    tag_name: None,
                    formatted_message: Some(error_message),
                }
            }],
        };
    }

    let (html, error) = match element.render(&render_options) {
        Ok(result) => (Some(result), None),
        Err(err) => (None, Some(err)),
    };

    let error_message = match error {
        Some(err) => Some(format!("Error rendering MJML: {err}")),
        None => None,
    };

    let errors = match error_message {
        Some(err) => vec![{
            RenderError {
                line: None,
                message: Some(err.clone()),
                tag_name: None,
                formatted_message: Some(err.clone()),
            }
        }],
        None => vec![],
    };

    RenderResult { html, errors }
}

#[cfg(test)]
mod tests {
    use super::*;
    use serde_json::json;
    use std::ffi::CString;

    #[test]
    fn test_render_mjml_null_input() {
        let result = render_mjml(std::ptr::null());
        assert!(result.is_null());
    }

    #[test]
    fn test_render_mjml_valid_template() {
        let input = json!({
            "directory": "/",
            "content": "<mjml><mj-body><mj-section><mj-column><mj-text>Hello World</mj-text></mj-column></mj-section></mj-body></mjml>",
            "options": {
                "mjmlConfigPath": null
            }
        });
        let c_template = CString::new(input.to_string()).unwrap();
        let result = render_mjml(c_template.as_ptr());

        assert!(!result.is_null());

        // Convert result back to Rust string and parse JSON
        let c_str = unsafe { CStr::from_ptr(result) };
        let json_str = c_str.to_str().unwrap();
        let render_result: RenderResult = serde_json::from_str(json_str).unwrap();

        assert!(render_result.html.is_some());
        assert!(render_result.errors.is_empty());
        assert!(render_result.html.unwrap().contains("Hello World"));

        // Clean up
        crate::memory_management::free_string(result);
    }

    #[test]
    fn test_render_mjml_empty_template() {
        let input = json!({
            "directory": "/",
            "content": "",
            "options": {
                "mjmlConfigPath": null
            }
        });
        let c_template = CString::new(input.to_string()).unwrap();
        let result = render_mjml(c_template.as_ptr());

        assert!(!result.is_null());

        let c_str = unsafe { CStr::from_ptr(result) };
        let json_str = c_str.to_str().unwrap();
        let render_result: RenderResult = serde_json::from_str(json_str).unwrap();

        assert!(render_result.html.is_none());
        assert!(!render_result.errors.is_empty());

        // Clean up
        crate::memory_management::free_string(result);
    }

    #[test]
    fn test_render_mjml_result_valid_template() {
        let template = r#"<mjml><mj-body><mj-section><mj-column><mj-text>Hello World</mj-text></mj-column></mj-section></mj-body></mjml>"#;
        let result = render_mjml_result(RenderInput {
            content: String::from(template),
            directory: String::from("/"),
            options: RenderInputOptions {
                mjml_config_path: None,
            },
        });

        assert!(result.html.is_some());
        assert!(result.errors.is_empty());
        assert!(result.html.unwrap().contains("Hello World"));
    }

    #[test]
    fn test_render_mjml_result_valid_template_with_include() {
        let template = r#"<mjml><mj-body><mj-section><mj-column><mj-text>Hello World</mj-text><mj-include path="path.mjml" /></mj-column></mj-section></mj-body></mjml>"#;
        let folder = std::env::current_dir().unwrap();
        let result = render_mjml_result(RenderInput {
            content: String::from(template),
            directory: String::from(folder.join("test-fixtures").to_str().unwrap()),
            options: RenderInputOptions {
                mjml_config_path: None,
            },
        });

        assert!(result.html.is_some());
        assert!(result.errors.is_empty());
        assert!(result.html.unwrap().contains("path content"));
    }

    #[test]
    fn test_render_mjml_result_valid_template_with_nested_include() {
        let template = r#"<mjml><mj-body><mj-section><mj-column><mj-text>Hello World</mj-text><mj-include path="nested/path.mjml" /></mj-column></mj-section></mj-body></mjml>"#;
        let folder = std::env::current_dir().unwrap();
        let result = render_mjml_result(RenderInput {
            content: String::from(template),
            directory: String::from(folder.join("test-fixtures").to_str().unwrap()),
            options: RenderInputOptions {
                mjml_config_path: None,
            },
        });

        assert!(result.html.is_some());
        assert!(result.errors.is_empty());
        assert!(result.html.unwrap().contains("nested include content"));
    }

    #[test]
    fn test_render_mjml_result_valid_template_with_relative_include() {
        let template = r#"<mjml><mj-body><mj-section><mj-column><mj-text>Hello World</mj-text><mj-include path="../path.mjml" /></mj-column></mj-section></mj-body></mjml>"#;
        let folder = std::env::current_dir().unwrap();
        let result = render_mjml_result(RenderInput {
            content: String::from(template),
            directory: String::from(
                folder
                    .join("test-fixtures")
                    .join("nested")
                    .to_str()
                    .unwrap(),
            ),
            options: RenderInputOptions {
                mjml_config_path: None,
            },
        });

        assert!(result.html.is_some());
        assert!(result.errors.is_empty());
        assert!(result.html.unwrap().contains("path content"));
    }

    #[test]
    fn test_render_mjml_result_malformed_template() {
        let template = r#"<mjml><unclosed-tag>"#;
        let result = render_mjml_result(RenderInput {
            content: String::from(template),
            directory: String::from("/"),
            options: RenderInputOptions {
                mjml_config_path: None,
            },
        });

        assert!(result.html.is_none());
        assert!(!result.errors.is_empty());
        assert!(
            result.errors[0]
                .message
                .as_ref()
                .unwrap()
                .contains("Error parsing MJML")
        );
    }

    #[test]
    fn test_render_mjml_result_with_invalid_attributes() {
        let template = r#"<mjml><mj-body><mj-section invalid-attr="test"><mj-column><mj-text>Test</mj-text></mj-column></mj-section></mj-body></mjml>"#;
        let result = render_mjml_result(RenderInput {
            content: String::from(template),
            directory: String::from("/"),
            options: RenderInputOptions {
                mjml_config_path: None,
            },
        });

        // The MJML renderer might still generate HTML despite invalid attributes
        if result.html.is_some() {
            assert!(result.html.unwrap().contains("Test"));
        }
    }
}
