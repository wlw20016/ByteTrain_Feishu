use crate::async_api::MockFeedSdk;
use crate::error::SdkError;
use crate::protobuf::{
    decode_page_request, encode_mail_page_response, encode_message_page_response,
};

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
#[repr(u8)]
pub enum BridgeErrorCode {
    InvalidPageSize = 1,
    InvalidCursor = 2,
    CursorOutOfRange = 3,
    ProtobufDecode = 4,
    ProtobufEncode = 5,
    SdkRead = 6,
    NativeBridge = 7,
}

impl BridgeErrorCode {
    fn as_u8(self) -> u8 {
        self as u8
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct BridgeError {
    pub code: BridgeErrorCode,
    pub message: String,
}

pub type BridgeResult<T> = Result<T, BridgeError>;

pub fn read_message_page_response_bytes(request_bytes: &[u8]) -> BridgeResult<Vec<u8>> {
    let request = decode_page_request(request_bytes).map_err(BridgeError::from_sdk_error)?;
    let page = MockFeedSdk::default()
        .get_message_page(request.page_size, request.cursor.as_deref())
        .map_err(BridgeError::from_sdk_error)?;

    encode_message_page_response(&page).map_err(BridgeError::from_sdk_error)
}

pub fn read_mail_page_response_bytes(request_bytes: &[u8]) -> BridgeResult<Vec<u8>> {
    let request = decode_page_request(request_bytes).map_err(BridgeError::from_sdk_error)?;
    let page = MockFeedSdk::default()
        .get_mail_page(request.page_size, request.cursor.as_deref())
        .map_err(BridgeError::from_sdk_error)?;

    encode_mail_page_response(&page).map_err(BridgeError::from_sdk_error)
}

impl BridgeError {
    fn from_sdk_error(error: SdkError) -> Self {
        let code = match &error {
            SdkError::InvalidPageSize { .. } => BridgeErrorCode::InvalidPageSize,
            SdkError::InvalidCursor { .. } => BridgeErrorCode::InvalidCursor,
            SdkError::CursorOutOfRange { .. } => BridgeErrorCode::CursorOutOfRange,
            SdkError::ProtobufDecode { .. } => BridgeErrorCode::ProtobufDecode,
            SdkError::ProtobufEncode { .. } => BridgeErrorCode::ProtobufEncode,
            SdkError::AsyncRead { .. } => BridgeErrorCode::SdkRead,
        };

        Self {
            code,
            message: error.to_string(),
        }
    }

    fn native(message: impl Into<String>) -> Self {
        Self {
            code: BridgeErrorCode::NativeBridge,
            message: message.into(),
        }
    }
}

fn encode_bridge_success(payload: &[u8]) -> Vec<u8> {
    let mut output = Vec::with_capacity(payload.len() + 1);
    output.push(0);
    output.extend_from_slice(payload);
    output
}

fn encode_bridge_error(error: BridgeError) -> Vec<u8> {
    let message = error.message.as_bytes();
    let message_len = u32::try_from(message.len()).unwrap_or(u32::MAX);
    let truncated_message = &message[..message_len as usize];

    let mut output = Vec::with_capacity(truncated_message.len() + 6);
    output.push(1);
    output.push(error.code.as_u8());
    output.extend_from_slice(&message_len.to_be_bytes());
    output.extend_from_slice(truncated_message);
    output
}

fn encode_bridge_result(result: BridgeResult<Vec<u8>>) -> Vec<u8> {
    match result {
        Ok(payload) => encode_bridge_success(&payload),
        Err(error) => encode_bridge_error(error),
    }
}

#[allow(non_camel_case_types)]
type jsize = i32;
#[allow(non_camel_case_types)]
type jbyte = i8;
#[allow(non_camel_case_types)]
type jarray = *mut std::ffi::c_void;
#[allow(non_camel_case_types)]
type jbyteArray = *mut std::ffi::c_void;
#[allow(non_camel_case_types)]
type jobject = *mut std::ffi::c_void;

#[repr(C)]
pub(crate) struct JNINativeInterface {
    reserved0: *mut std::ffi::c_void,
    reserved1: *mut std::ffi::c_void,
    reserved2: *mut std::ffi::c_void,
    reserved3: *mut std::ffi::c_void,
    get_version: *mut std::ffi::c_void,
    define_class: *mut std::ffi::c_void,
    find_class: *mut std::ffi::c_void,
    from_reflected_method: *mut std::ffi::c_void,
    from_reflected_field: *mut std::ffi::c_void,
    to_reflected_method: *mut std::ffi::c_void,
    get_superclass: *mut std::ffi::c_void,
    is_assignable_from: *mut std::ffi::c_void,
    to_reflected_field: *mut std::ffi::c_void,
    throw: *mut std::ffi::c_void,
    throw_new: *mut std::ffi::c_void,
    exception_occurred: *mut std::ffi::c_void,
    exception_describe: *mut std::ffi::c_void,
    exception_clear: *mut std::ffi::c_void,
    fatal_error: *mut std::ffi::c_void,
    push_local_frame: *mut std::ffi::c_void,
    pop_local_frame: *mut std::ffi::c_void,
    new_global_ref: *mut std::ffi::c_void,
    delete_global_ref: *mut std::ffi::c_void,
    delete_local_ref: *mut std::ffi::c_void,
    is_same_object: *mut std::ffi::c_void,
    new_local_ref: *mut std::ffi::c_void,
    ensure_local_capacity: *mut std::ffi::c_void,
    alloc_object: *mut std::ffi::c_void,
    new_object: *mut std::ffi::c_void,
    new_object_v: *mut std::ffi::c_void,
    new_object_a: *mut std::ffi::c_void,
    get_object_class: *mut std::ffi::c_void,
    is_instance_of: *mut std::ffi::c_void,
    get_method_id: *mut std::ffi::c_void,
    call_object_method: *mut std::ffi::c_void,
    call_object_method_v: *mut std::ffi::c_void,
    call_object_method_a: *mut std::ffi::c_void,
    call_boolean_method: *mut std::ffi::c_void,
    call_boolean_method_v: *mut std::ffi::c_void,
    call_boolean_method_a: *mut std::ffi::c_void,
    call_byte_method: *mut std::ffi::c_void,
    call_byte_method_v: *mut std::ffi::c_void,
    call_byte_method_a: *mut std::ffi::c_void,
    call_char_method: *mut std::ffi::c_void,
    call_char_method_v: *mut std::ffi::c_void,
    call_char_method_a: *mut std::ffi::c_void,
    call_short_method: *mut std::ffi::c_void,
    call_short_method_v: *mut std::ffi::c_void,
    call_short_method_a: *mut std::ffi::c_void,
    call_int_method: *mut std::ffi::c_void,
    call_int_method_v: *mut std::ffi::c_void,
    call_int_method_a: *mut std::ffi::c_void,
    call_long_method: *mut std::ffi::c_void,
    call_long_method_v: *mut std::ffi::c_void,
    call_long_method_a: *mut std::ffi::c_void,
    call_float_method: *mut std::ffi::c_void,
    call_float_method_v: *mut std::ffi::c_void,
    call_float_method_a: *mut std::ffi::c_void,
    call_double_method: *mut std::ffi::c_void,
    call_double_method_v: *mut std::ffi::c_void,
    call_double_method_a: *mut std::ffi::c_void,
    call_void_method: *mut std::ffi::c_void,
    call_void_method_v: *mut std::ffi::c_void,
    call_void_method_a: *mut std::ffi::c_void,
    call_nonvirtual_object_method: *mut std::ffi::c_void,
    call_nonvirtual_object_method_v: *mut std::ffi::c_void,
    call_nonvirtual_object_method_a: *mut std::ffi::c_void,
    call_nonvirtual_boolean_method: *mut std::ffi::c_void,
    call_nonvirtual_boolean_method_v: *mut std::ffi::c_void,
    call_nonvirtual_boolean_method_a: *mut std::ffi::c_void,
    call_nonvirtual_byte_method: *mut std::ffi::c_void,
    call_nonvirtual_byte_method_v: *mut std::ffi::c_void,
    call_nonvirtual_byte_method_a: *mut std::ffi::c_void,
    call_nonvirtual_char_method: *mut std::ffi::c_void,
    call_nonvirtual_char_method_v: *mut std::ffi::c_void,
    call_nonvirtual_char_method_a: *mut std::ffi::c_void,
    call_nonvirtual_short_method: *mut std::ffi::c_void,
    call_nonvirtual_short_method_v: *mut std::ffi::c_void,
    call_nonvirtual_short_method_a: *mut std::ffi::c_void,
    call_nonvirtual_int_method: *mut std::ffi::c_void,
    call_nonvirtual_int_method_v: *mut std::ffi::c_void,
    call_nonvirtual_int_method_a: *mut std::ffi::c_void,
    call_nonvirtual_long_method: *mut std::ffi::c_void,
    call_nonvirtual_long_method_v: *mut std::ffi::c_void,
    call_nonvirtual_long_method_a: *mut std::ffi::c_void,
    call_nonvirtual_float_method: *mut std::ffi::c_void,
    call_nonvirtual_float_method_v: *mut std::ffi::c_void,
    call_nonvirtual_float_method_a: *mut std::ffi::c_void,
    call_nonvirtual_double_method: *mut std::ffi::c_void,
    call_nonvirtual_double_method_v: *mut std::ffi::c_void,
    call_nonvirtual_double_method_a: *mut std::ffi::c_void,
    call_nonvirtual_void_method: *mut std::ffi::c_void,
    call_nonvirtual_void_method_v: *mut std::ffi::c_void,
    call_nonvirtual_void_method_a: *mut std::ffi::c_void,
    get_field_id: *mut std::ffi::c_void,
    get_object_field: *mut std::ffi::c_void,
    get_boolean_field: *mut std::ffi::c_void,
    get_byte_field: *mut std::ffi::c_void,
    get_char_field: *mut std::ffi::c_void,
    get_short_field: *mut std::ffi::c_void,
    get_int_field: *mut std::ffi::c_void,
    get_long_field: *mut std::ffi::c_void,
    get_float_field: *mut std::ffi::c_void,
    get_double_field: *mut std::ffi::c_void,
    set_object_field: *mut std::ffi::c_void,
    set_boolean_field: *mut std::ffi::c_void,
    set_byte_field: *mut std::ffi::c_void,
    set_char_field: *mut std::ffi::c_void,
    set_short_field: *mut std::ffi::c_void,
    set_int_field: *mut std::ffi::c_void,
    set_long_field: *mut std::ffi::c_void,
    set_float_field: *mut std::ffi::c_void,
    set_double_field: *mut std::ffi::c_void,
    get_static_method_id: *mut std::ffi::c_void,
    call_static_object_method: *mut std::ffi::c_void,
    call_static_object_method_v: *mut std::ffi::c_void,
    call_static_object_method_a: *mut std::ffi::c_void,
    call_static_boolean_method: *mut std::ffi::c_void,
    call_static_boolean_method_v: *mut std::ffi::c_void,
    call_static_boolean_method_a: *mut std::ffi::c_void,
    call_static_byte_method: *mut std::ffi::c_void,
    call_static_byte_method_v: *mut std::ffi::c_void,
    call_static_byte_method_a: *mut std::ffi::c_void,
    call_static_char_method: *mut std::ffi::c_void,
    call_static_char_method_v: *mut std::ffi::c_void,
    call_static_char_method_a: *mut std::ffi::c_void,
    call_static_short_method: *mut std::ffi::c_void,
    call_static_short_method_v: *mut std::ffi::c_void,
    call_static_short_method_a: *mut std::ffi::c_void,
    call_static_int_method: *mut std::ffi::c_void,
    call_static_int_method_v: *mut std::ffi::c_void,
    call_static_int_method_a: *mut std::ffi::c_void,
    call_static_long_method: *mut std::ffi::c_void,
    call_static_long_method_v: *mut std::ffi::c_void,
    call_static_long_method_a: *mut std::ffi::c_void,
    call_static_float_method: *mut std::ffi::c_void,
    call_static_float_method_v: *mut std::ffi::c_void,
    call_static_float_method_a: *mut std::ffi::c_void,
    call_static_double_method: *mut std::ffi::c_void,
    call_static_double_method_v: *mut std::ffi::c_void,
    call_static_double_method_a: *mut std::ffi::c_void,
    call_static_void_method: *mut std::ffi::c_void,
    call_static_void_method_v: *mut std::ffi::c_void,
    call_static_void_method_a: *mut std::ffi::c_void,
    get_static_field_id: *mut std::ffi::c_void,
    get_static_object_field: *mut std::ffi::c_void,
    get_static_boolean_field: *mut std::ffi::c_void,
    get_static_byte_field: *mut std::ffi::c_void,
    get_static_char_field: *mut std::ffi::c_void,
    get_static_short_field: *mut std::ffi::c_void,
    get_static_int_field: *mut std::ffi::c_void,
    get_static_long_field: *mut std::ffi::c_void,
    get_static_float_field: *mut std::ffi::c_void,
    get_static_double_field: *mut std::ffi::c_void,
    set_static_object_field: *mut std::ffi::c_void,
    set_static_boolean_field: *mut std::ffi::c_void,
    set_static_byte_field: *mut std::ffi::c_void,
    set_static_char_field: *mut std::ffi::c_void,
    set_static_short_field: *mut std::ffi::c_void,
    set_static_int_field: *mut std::ffi::c_void,
    set_static_long_field: *mut std::ffi::c_void,
    set_static_float_field: *mut std::ffi::c_void,
    set_static_double_field: *mut std::ffi::c_void,
    new_string: *mut std::ffi::c_void,
    get_string_length: *mut std::ffi::c_void,
    get_string_chars: *mut std::ffi::c_void,
    release_string_chars: *mut std::ffi::c_void,
    new_string_utf: *mut std::ffi::c_void,
    get_string_utf_length: *mut std::ffi::c_void,
    get_string_utf_chars: *mut std::ffi::c_void,
    release_string_utf_chars: *mut std::ffi::c_void,
    get_array_length: unsafe extern "system" fn(JNIEnv, jarray) -> jsize,
    new_object_array: *mut std::ffi::c_void,
    get_object_array_element: *mut std::ffi::c_void,
    set_object_array_element: *mut std::ffi::c_void,
    new_boolean_array: *mut std::ffi::c_void,
    new_byte_array: unsafe extern "system" fn(JNIEnv, jsize) -> jbyteArray,
    new_char_array: *mut std::ffi::c_void,
    new_short_array: *mut std::ffi::c_void,
    new_int_array: *mut std::ffi::c_void,
    new_long_array: *mut std::ffi::c_void,
    new_float_array: *mut std::ffi::c_void,
    new_double_array: *mut std::ffi::c_void,
    get_boolean_array_elements: *mut std::ffi::c_void,
    get_byte_array_elements: *mut std::ffi::c_void,
    get_char_array_elements: *mut std::ffi::c_void,
    get_short_array_elements: *mut std::ffi::c_void,
    get_int_array_elements: *mut std::ffi::c_void,
    get_long_array_elements: *mut std::ffi::c_void,
    get_float_array_elements: *mut std::ffi::c_void,
    get_double_array_elements: *mut std::ffi::c_void,
    release_boolean_array_elements: *mut std::ffi::c_void,
    release_byte_array_elements: *mut std::ffi::c_void,
    release_char_array_elements: *mut std::ffi::c_void,
    release_short_array_elements: *mut std::ffi::c_void,
    release_int_array_elements: *mut std::ffi::c_void,
    release_long_array_elements: *mut std::ffi::c_void,
    release_float_array_elements: *mut std::ffi::c_void,
    release_double_array_elements: *mut std::ffi::c_void,
    get_boolean_array_region: *mut std::ffi::c_void,
    get_byte_array_region:
        unsafe extern "system" fn(JNIEnv, jbyteArray, jsize, jsize, *mut jbyte),
    get_char_array_region: *mut std::ffi::c_void,
    get_short_array_region: *mut std::ffi::c_void,
    get_int_array_region: *mut std::ffi::c_void,
    get_long_array_region: *mut std::ffi::c_void,
    get_float_array_region: *mut std::ffi::c_void,
    get_double_array_region: *mut std::ffi::c_void,
    set_boolean_array_region: *mut std::ffi::c_void,
    set_byte_array_region:
        unsafe extern "system" fn(JNIEnv, jbyteArray, jsize, jsize, *const jbyte),
}

type JNIEnv = *mut *mut JNINativeInterface;

unsafe fn read_jbyte_array(env: JNIEnv, request_bytes: jbyteArray) -> BridgeResult<Vec<u8>> {
    if env.is_null() || request_bytes.is_null() {
        return Err(BridgeError::native("JNI environment or request byte array was null"));
    }
    let functions = *env;
    if functions.is_null() {
        return Err(BridgeError::native("JNI native function table was null"));
    }

    let len = ((*functions).get_array_length)(env, request_bytes as jarray);
    if len < 0 {
        return Err(BridgeError::native("JNI byte array length was negative"));
    }

    let mut output = vec![0u8; len as usize];
    ((*functions).get_byte_array_region)(
        env,
        request_bytes,
        0,
        len,
        output.as_mut_ptr() as *mut jbyte,
    );
    Ok(output)
}

unsafe fn write_jbyte_array(env: JNIEnv, payload: &[u8]) -> jbyteArray {
    if env.is_null() {
        return std::ptr::null_mut();
    }
    let functions = *env;
    if functions.is_null() {
        return std::ptr::null_mut();
    }

    let Ok(len) = jsize::try_from(payload.len()) else {
        return std::ptr::null_mut();
    };
    let array = ((*functions).new_byte_array)(env, len);
    if array.is_null() {
        return std::ptr::null_mut();
    }
    ((*functions).set_byte_array_region)(env, array, 0, len, payload.as_ptr() as *const jbyte);
    array
}

#[no_mangle]
pub extern "system" fn Java_com_bytetrain_feishuclone_sdk_NativeRustFeedBridge_readMessagePageNative(
    env: JNIEnv,
    _object: jobject,
    request_bytes: jbyteArray,
) -> jbyteArray {
    let result = unsafe { read_jbyte_array(env, request_bytes) }
        .and_then(|bytes| read_message_page_response_bytes(&bytes));
    let envelope = encode_bridge_result(result);
    unsafe { write_jbyte_array(env, &envelope) }
}

#[no_mangle]
pub extern "system" fn Java_com_bytetrain_feishuclone_sdk_NativeRustFeedBridge_readMailPageNative(
    env: JNIEnv,
    _object: jobject,
    request_bytes: jbyteArray,
) -> jbyteArray {
    let result = unsafe { read_jbyte_array(env, request_bytes) }
        .and_then(|bytes| read_mail_page_response_bytes(&bytes));
    let envelope = encode_bridge_result(result);
    unsafe { write_jbyte_array(env, &envelope) }
}
