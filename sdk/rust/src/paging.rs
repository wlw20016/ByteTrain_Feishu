use crate::error::{SdkError, SdkResult};
use crate::models::Page;

pub const DEFAULT_TOTAL_COUNT: usize = 10_000;
pub const MIN_PAGE_SIZE: usize = 1;
pub const MAX_PAGE_SIZE: usize = 200;

pub(crate) fn paginate<T: Clone>(
    items: &[T],
    page_size: usize,
    cursor: Option<&str>,
) -> SdkResult<Page<T>> {
    validate_page_size(page_size)?;

    let start_index = parse_cursor(cursor, items.len())?;
    let end_index = start_index.saturating_add(page_size).min(items.len());
    let page_items = items[start_index..end_index].to_vec();
    let has_more = end_index < items.len();

    Ok(Page {
        items: page_items,
        next_cursor: has_more.then(|| end_index.to_string()),
        has_more,
    })
}

fn validate_page_size(page_size: usize) -> SdkResult<()> {
    if (MIN_PAGE_SIZE..=MAX_PAGE_SIZE).contains(&page_size) {
        Ok(())
    } else {
        Err(SdkError::InvalidPageSize {
            page_size,
            min: MIN_PAGE_SIZE,
            max: MAX_PAGE_SIZE,
        })
    }
}

fn parse_cursor(cursor: Option<&str>, total_count: usize) -> SdkResult<usize> {
    match cursor {
        None | Some("") => Ok(0),
        Some(value) => {
            let start_index = value
                .parse::<usize>()
                .map_err(|_| SdkError::InvalidCursor {
                    cursor: value.to_owned(),
                })?;

            if start_index <= total_count {
                Ok(start_index)
            } else {
                Err(SdkError::CursorOutOfRange {
                    cursor: value.to_owned(),
                    total_count,
                })
            }
        }
    }
}
