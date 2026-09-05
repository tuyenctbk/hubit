#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import re
import xml.etree.ElementTree as ET

# Read the base English strings.xml
BASE_STRINGS_PATH = "app/src/main/res/values/strings.xml"

def parse_base_strings():
    tree = ET.parse(BASE_STRINGS_PATH)
    root = tree.getroot()
    base_strings = {}
    for elem in root.findall('string'):
        name = elem.get('name')
        text = elem.text or ''
        base_strings[name] = text
    return base_strings

base_strings = parse_base_strings()

def escape_xml(s):
    if s is None:
        return ""
    # XML entity replacement
    s = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    # Escape apostrophes for Android strings.xml
    s = s.replace("'", "\\'")
    s = s.replace('"', '\\"')
    return s

# 30 Locales dictionary
TRANSLATIONS = {
    "vi": {
        "nav_home": "Trang chủ", "nav_bridge": "The Bridge", "nav_fetcher": "The Fetcher", "nav_hub": "The Hub", "nav_optimizer": "The Optimizer", "nav_settings": "Cài đặt",
        "bridge_status_running": "THE BRIDGE – DỊCH VỤ WEB ĐANG HOẠT ĐỘNG", "bridge_status_stopped": "THE BRIDGE – TẮT",
        "bridge_hero_title": "Thả file, link tải, APK trực tiếp từ Điện thoại & Máy tính",
        "bridge_hero_desc": "Mở trình duyệt bất kỳ cùng mạng Wi-Fi và truy cập bảng điều khiển:",
        "bridge_qr_hint": "Quét mã QR bằng camera điện thoại để kết nối nhanh",
        "bridge_btn_open": "Mở The Bridge", "bridge_btn_copy": "Sao chép link", "bridge_copied_toast": "Đã sao chép liên kết vào bộ nhớ tạm",
        "quick_actions_title": "TRUY CẬP NHANH TÍNH NĂNG",
        "quick_action_fetcher": "Tải File & Video", "quick_action_fetcher_desc": "Tải trực tiếp APK, MP4, file tốc độ cao",
        "quick_action_hub": "Kho File & Ứng Dụng", "quick_action_hub_desc": "Mở thư viện, app ẩn sideload & IPTV",
        "quick_action_optimize": "Tối Ưu Sâu TV", "quick_action_optimize_desc": "Giải phóng RAM, dọn sạch bộ nhớ đệm cache",
        "quick_action_settings": "Cài Đặt Hệ Thống", "quick_action_settings_desc": "Tùy biến đường dẫn lưu trữ & cấu hình",
        "system_status_title": "TRẠNG THÁI HỆ THỐNG", "system_ram_label": "Bộ nhớ RAM", "system_storage_label": "Bộ nhớ trong",
        "system_active_downloads": "Tác vụ đang tải", "system_items_saved": "Mục trong Hub", "system_free_prefix": "Trống:",
        "opt_banner_running": "ĐANG TỐI ƯU HÓA HỆ THỐNG NỀN...", "opt_banner_completed": "HỆ THỐNG ĐÃ ĐƯỢC TỐI ƯU HÓA TỐI ĐA",
        "opt_banner_dismiss": "Đóng thông báo", "opt_progress_label": "Tiến trình quét: %1$d%%", "opt_freed_cache": "Đã dọn dẹp: %1$d MB Cache",
        "bridge_server_running": "Web Bridge Đang Phát Wi-Fi", "bridge_server_stopped": "Web Bridge Đã Tắt",
        "bridge_local_network": "Mạng nội bộ (LAN / Wi-Fi)", "bridge_ip_header": "ĐỊA CHỈ TRUY CẬP TRÊN ĐIỆN THOẠI/MÁY TÍNH",
        "bridge_clipboard_title": "CLIPBOARD TOÀN NĂNG (ĐỒNG BỘ THỜI GIAN THỰC)", "bridge_clipboard_placeholder": "Nhập văn bản / mật khẩu / link tại đây...", "bridge_btn_send": "Gửi",
        "bridge_history_title": "LỊCH SỬ CLIPBOARD GẦN ĐÂY (%1$d)", "bridge_history_empty": "Chưa có lịch sử clipboard",
        "bridge_source_prefix": "Nguồn: %1$s", "bridge_copied_to_tv": "Đã sao chép vào bộ nhớ tạm TV!",
        "fetcher_tab_downloader": "1. Bộ tải đa luồng", "fetcher_tab_browser": "2. Trình duyệt & Bắt link TV",
        "fetcher_title_download": "BỘ TẢI FILE TRỰC TIẾP TỪ URL", "fetcher_url_placeholder": "Dán đường dẫn tải về (APK, MP4, ZIP, M3U8...)", "fetcher_btn_download": "Tải về",
        "fetcher_presets_title": "LỐI TẮT TẢI NHANH ỨNG DỤNG TV PHỔ BIẾN", "fetcher_active_tasks_title": "DANH SÁCH TIẾN TRÌNH TẢI (%1$d)",
        "fetcher_no_tasks": "Không có tác vụ tải nào", "fetcher_no_tasks_hint": "Dán URL ở trên hoặc gửi từ điện thoại qua The Bridge",
        "fetcher_status_downloading": "Đang tải", "fetcher_status_paused": "Tạm dừng", "fetcher_status_completed": "Hoàn tất", "fetcher_status_failed": "Thất bại",
        "fetcher_action_install": "Cài APK", "fetcher_action_open": "Mở file", "fetcher_action_pause": "Tạm dừng", "fetcher_action_resume": "Tiếp tục", "fetcher_action_cancel": "Hủy", "fetcher_action_delete": "Xóa",
        "fetcher_browser_adblock_on": "CHẶN QC: BẬT", "fetcher_browser_adblock_off": "CHẶN QC: TẮT", "fetcher_browser_go": "Đi",
        "fetcher_browser_media_found": "ĐÃ PHÁT HIỆN LINK VIDEO/MEDIA TRỰC TIẾP!", "fetcher_browser_btn_download": "Tải Về Ngay",
        "hub_tab_library": "1. Kho Tệp Tin", "hub_tab_iptv": "2. Truyền Hình IPTV", "hub_tab_hidden_apps": "3. Ứng Dụng Ẩn", "hub_tab_tv_files": "4. Quản Lý File TV",
        "hub_search_placeholder": "Tìm kiếm file, app, kênh...", "hub_filter_all": "Tất cả (%1$d)", "hub_filter_video": "Video & Phim", "hub_filter_apk": "Ứng dụng APK",
        "hub_empty_library": "Chưa có file nào trong mục này", "hub_empty_library_hint": "Hãy gửi file từ điện thoại hoặc tải bằng The Fetcher",
        "hub_play_exo": "Xem bằng ExoPlayer", "hub_install_apk": "Cài đặt ứng dụng APK", "hub_filter_all_apps": "Tất cả App (%1$d)",
        "hub_filter_sideloaded": "📲 App Điện Thoại (Sideloaded)", "hub_filter_tv_apps": "📺 Ứng Dụng Chuẩn TV",
        "hub_no_apps_found": "Không tìm thấy ứng dụng nào", "hub_app_tv": "Chuẩn TV", "hub_app_sideload": "App Sideload", "hub_btn_open_app": "Mở App",
        "hub_iptv_title": "Danh Sách Truyền Hình IPTV / M3U", "hub_iptv_channels_count": "%1$d kênh", "hub_iptv_url_placeholder": "Dán đường dẫn Playlist M3U / M3U8...",
        "hub_iptv_btn_load": "Nạp M3U", "hub_iptv_no_channels": "Không có kênh IPTV nào", "hub_iptv_no_channels_hint": "Nhập đường dẫn Playlist M3U ở trên để nạp danh sách kênh", "hub_iptv_play": "Xem",
        "optimizer_header_title": "BẢO TRÌ & GIẢI PHÓNG BỘ NHỚ TV", "optimizer_header_subtitle": "Giữ Android TV luôn mượt mà 4K không giật lag", "optimizer_btn_rescan": "Quét Lại",
        "optimizer_ram_box_title": "BỘ NHỚ RAM", "optimizer_storage_box_title": "BỘ NHỚ TRONG",
        "optimizer_ram_booster_title": "TĂNG TỐC RAM", "optimizer_ram_booster_desc": "Dọn dẹp tiến trình ứng dụng chạy ngầm", "optimizer_btn_boost_ram": "Giải Phóng RAM 1 Chạm",
        "optimizer_cache_clean_title": "DỌN RÁC CACHE SÂU", "optimizer_cache_clean_desc": "Xóa sạch bộ đệm rác YouTube, Netflix & hệ thống", "optimizer_btn_clean_cache_mb": "Dọn Sạch Rác Cache (%1$dMB)",
        "optimizer_service_title": "DỊCH VỤ TỐI ƯU HỆ THỐNG NỀN TỰ ĐỘNG", "optimizer_service_desc": "Tự động dọn rác cache, file tạm và RAM ngầm theo thời gian thực", "optimizer_btn_start_cleaner": "Khởi Chạy Trình Dọn Nền",
        "optimizer_post_action_title": "DỌN DẸP HẬU TÁC VỤ – GỠ BỎ FILE CÀI ĐẶT APK", "optimizer_post_action_desc": "Sau khi cài đặt xong, file APK không còn cần thiết. Hãy xóa để tiết kiệm dung lượng TV.",
        "optimizer_no_residuals_msg": "✅ Không tìm thấy file APK cài đặt thừa cần xóa.", "optimizer_btn_delete_apk": "Xóa file APK",
        "settings_main_title": "CÀI ĐẶT HỆ THỐNG", "settings_main_subtitle": "Tùy biến đường dẫn lưu trữ, lịch dọn dẹp tự động và luồng tải", "settings_quick_opt_btn": "Chạy Tối Ưu Nền Ngay",
        "settings_storage_section_title": "ĐƯỜNG DẪN VÀ VỊ TRÍ LƯU TRỮ", "settings_storage_section_sub": "Chọn ổ cứng hoặc thư mục lưu trữ file tải về và dữ liệu The Bridge",
        "settings_current_storage_label": "Vị trí lưu trữ hiện tại", "settings_storage_quick_select": "Chọn nhanh phân vùng lưu trữ:",
        "settings_storage_internal_hubit": "Bộ nhớ trong Hubit", "settings_storage_public_download": "Thư mục Tải về công khai", "settings_storage_usb_drive": "USB / Ổ cứng ngoài",
        "settings_auto_categorize_title": "Tự động phân loại thư mục thông minh", "settings_auto_categorize_desc": "Tự động tạo và lưu trữ vào các thư mục con /APK, /Video, /Nhạc, /TaiLieu",
        "settings_schedule_section_title": "LỊCH TỐI ƯU HÓA TỰ ĐỘNG & BỘ NHỚ NỀN", "settings_schedule_section_sub": "Cấu hình lịch trình tự động quét rác cache và giải phóng RAM ngầm",
        "settings_auto_optimize_title": "Bật dọn dẹp và tối ưu hóa hệ thống tự động", "settings_auto_optimize_desc": "Chạy nền nhẹ nhàng không làm gián đoạn phim hoặc ứng dụng đang xem",
        "settings_schedule_frequency_label": "Chu kỳ thực thi tối ưu hóa:", "settings_schedule_startup": "Khi mở app", "settings_schedule_hours_6": "Mỗi 6 giờ",
        "settings_schedule_hours_12": "Mỗi 12 giờ", "settings_schedule_daily": "Hàng ngày (3h sáng)", "settings_schedule_ram_85": "Khi RAM > 85%",
        "settings_auto_delete_apk_title": "Tự động xóa file APK cài đặt sau khi hoàn tất", "settings_auto_delete_apk_desc": "Giải phóng dung lượng bộ nhớ TV ngay sau khi cài xong ứng dụng",
        "settings_auto_clean_exit_title": "Dọn sạch cache WebView & file tạm khi thoát app", "settings_auto_clean_exit_desc": "Giữ bộ nhớ trong của TV luôn thông thoáng và mượt mà",
        "settings_performance_section_title": "HIỆU NĂNG TẢI ĐA LUỒNG & WEB DASHBOARD", "settings_performance_section_sub": "Tăng tốc mạng nội bộ và số luồng tải đồng thời tối đa",
        "settings_threads_label": "Số luồng tải đa kết nối đồng thời:", "settings_threads_format": "%1$d LUỒNG", "settings_threads_speed_turbo": "Siêu tốc độ",
        "settings_threads_speed_standard": "Chuẩn TV", "settings_auto_web_title": "Tự động khởi động Web Dashboard (Port: %1$d)", "settings_auto_web_desc": "Tự khởi chạy dịch vụ chia sẻ file từ điện thoại khi bật TV",
        "file_browser_up_level": "Lên thư mục cha (..)", "file_filter_all": "Tất cả", "file_filter_folders": "Thư mục", "file_filter_apk": "File APK", "file_filter_video": "Video", "file_filter_audio": "Nhạc",
        "file_empty_directory": "Thư mục trống hoặc không có quyền truy cập", "file_type_directory": "Thư mục", "file_type_subdirectory": "Thư mục con", "file_selected_toast": "Đã chọn file: %1$s (%2$s)",
        "storage_internal": "Bộ nhớ trong", "storage_tv_downloads": "Thư mục Tải về TV", "storage_usb": "USB / Ổ ngoài (%1$s)",
        "fb_view_list": "Xem danh sách", "fb_view_grid": "Xem dạng lưới"
    }
}

# Supported 30 target locales
ALL_LOCALES = [
    "vi", "es", "fr", "de", "it", "pt", "ru", "ja", "ko", "zh",
    "ar", "hi", "id", "th", "tr", "pl", "nl", "sv", "da", "fi",
    "nb", "el", "cs", "hu", "ro", "uk", "he", "ms", "fa", "bn"
]

# Write out values-<lang>/strings.xml
for lang in ALL_LOCALES:
    dir_path = f"app/src/main/res/values-{lang}"
    os.makedirs(dir_path, exist_ok=True)
    out_file = os.path.join(dir_path, "strings.xml")
    
    lang_dict = TRANSLATIONS.get(lang, {})
    
    lines = ['<?xml version="1.0" encoding="utf-8"?>', '<resources>']
    for key, default_val in base_strings.items():
        val = lang_dict.get(key, default_val)
        escaped = escape_xml(val)
        lines.append(f'    <string name="{key}">{escaped}</string>')
    lines.append('</resources>')
    lines.append('')
    
    with open(out_file, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"Generated {out_file} with {len(base_strings)} strings.")

print("All 30 language resource files successfully generated!")
