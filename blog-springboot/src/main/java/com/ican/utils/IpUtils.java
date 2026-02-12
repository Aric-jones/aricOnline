package com.ican.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ican.model.dto.BiliIpInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP地址工具类
 *
 * <p>获取客户端真实IP地址和IP归属地信息</p>
 *
 * <h3>IP归属地获取策略：</h3>
 * <ol>
 *     <li>先查本地缓存（ConcurrentHashMap，避免重复请求）</li>
 *     <li>调用B站 IP 查询 API（主要来源）</li>
 *     <li>B站 API 失败时，降级到太平洋网络 IP 查询 API</li>
 *     <li>所有 API 都失败时返回 "未知"</li>
 * </ol>
 *
 * @author ican
 */
@Slf4j
public class IpUtils {

    /**
     * IP 归属地本地缓存（IP -> 归属地字符串）
     * 避免同一 IP 短时间内重复调用外部 API
     */
    private static final ConcurrentHashMap<String, String> IP_SOURCE_CACHE = new ConcurrentHashMap<>(256);

    /**
     * 缓存最大容量，防止内存泄漏
     */
    private static final int MAX_CACHE_SIZE = 4096;

    /**
     * HTTP 请求超时时间（毫秒）
     */
    private static final int CONNECT_TIMEOUT = 3000;
    private static final int READ_TIMEOUT = 5000;

    /**
     * 带超时控制的 RestTemplate（单例复用，避免每次创建）
     */
    private static final RestTemplate REST_TEMPLATE;

    static {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        REST_TEMPLATE = new RestTemplate(factory);
    }

    /**
     * 根据 IP 获取地理位置（带缓存 + 多 API 降级）
     *
     * @param ip IP 地址
     * @return 地理位置字符串，格式："国家|省份|城市|运营商"，失败返回 "未知"
     */
    public static String getIpSource(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return "未知";
        }
        // 本地 IP 直接返回
        if (isLocalIp(ip)) {
            return "本机地址";
        }
        // 1. 查缓存
        String cached = IP_SOURCE_CACHE.get(ip);
        if (cached != null) {
            return cached;
        }
        // 2. 调用B站 API（主）
        String result = getIpSourceFromBilibili(ip);
        // 3. 降级到太平洋网络 API
        if (result == null) {
            result = getIpSourceFromPcOnline(ip);
        }
        // 4. 兜底
        if (result == null) {
            result = "未知";
        }
        // 5. 写入缓存（防止缓存过大）
        if (IP_SOURCE_CACHE.size() < MAX_CACHE_SIZE) {
            IP_SOURCE_CACHE.put(ip, result);
        }
        return result;
    }

    /**
     * 判断是否为本机/内网 IP
     */
    private static boolean isLocalIp(String ip) {
        return "127.0.0.1".equals(ip)
                || "0:0:0:0:0:0:0:1".equals(ip)
                || "::1".equals(ip)
                || ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || ip.startsWith("172.16.")
                || ip.startsWith("172.17.")
                || ip.startsWith("172.18.")
                || ip.startsWith("172.19.")
                || ip.startsWith("172.20.")
                || ip.startsWith("172.21.")
                || ip.startsWith("172.22.")
                || ip.startsWith("172.23.")
                || ip.startsWith("172.24.")
                || ip.startsWith("172.25.")
                || ip.startsWith("172.26.")
                || ip.startsWith("172.27.")
                || ip.startsWith("172.28.")
                || ip.startsWith("172.29.")
                || ip.startsWith("172.30.")
                || ip.startsWith("172.31.")
                || ip.startsWith("169.254.");
    }

    /**
     * 调用 B站 IP 查询 API
     */
    private static String getIpSourceFromBilibili(String ip) {
        try {
            String url = "https://api.bilibili.com/x/web-interface/zone?ip=" + ip;
            String response = REST_TEMPLATE.getForObject(url, String.class);
            if (response == null || response.isEmpty()) {
                return null;
            }
            JSONObject json = JSON.parseObject(response);
            if (json.getInteger("code") == 0) {
                JSONObject data = json.getJSONObject("data");
                if (data != null) {
                    return buildSourceString(
                            data.getString("country"),
                            data.getString("province"),
                            data.getString("city"),
                            data.getString("isp")
                    );
                }
            }
        } catch (Exception e) {
            log.warn("B站 IP 查询 API 失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 调用太平洋网络 IP 查询 API（降级方案）
     */
    private static String getIpSourceFromPcOnline(String ip) {
        try {
            String url = "http://whois.pconline.com.cn/ipJson.jsp?ip=" + ip + "&json=true";
            String response = REST_TEMPLATE.getForObject(url, String.class);
            if (response == null || response.isEmpty()) {
                return null;
            }
            // 响应可能是 GBK 编码，需要转换
            JSONObject json = JSON.parseObject(response);
            String province = json.getString("pro");
            String city = json.getString("city");
            String addr = json.getString("addr");
            if (province != null || city != null || addr != null) {
                return buildSourceString(null, province, city, addr);
            }
        } catch (Exception e) {
            log.warn("太平洋网络 IP 查询 API 失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 构建 IP 来源字符串（安全拼接，跳过 null/空值）
     */
    private static String buildSourceString(String country, String province, String city, String isp) {
        StringBuilder sb = new StringBuilder();
        if (country != null && !country.isEmpty()) {
            sb.append(country);
        }
        if (province != null && !province.isEmpty() && !province.equals(country)) {
            if (sb.length() > 0) sb.append("|");
            sb.append(province);
        }
        if (city != null && !city.isEmpty() && !city.equals(province)) {
            if (sb.length() > 0) sb.append("|");
            sb.append(city);
        }
        if (isp != null && !isp.isEmpty()) {
            if (sb.length() > 0) sb.append("|");
            sb.append(isp);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * 清空 IP 来源缓存（用于测试或手动清理）
     */
    public static void clearCache() {
        IP_SOURCE_CACHE.clear();
    }

    /**
     * 获取当前缓存大小
     */
    public static int getCacheSize() {
        return IP_SOURCE_CACHE.size();
    }
}
