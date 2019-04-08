# A closer look at obtaining the APK

There is no useful instructions w/  the controller. No “search for $legit_app in the App Store” Just a QR code you’re meant to scan.

The QR code decodes to `http://www.hao2b.cn/69605`, which looks like some sort of short url redirect/analytics service, but for apps. 

When you do not specify a `User-Agent:`, you eventually get a page that links you to the Google Play store
[LED BLE - Apps on Google Play](https://play.google.com/store/apps/details?id=com.ledble).  But if you load the link with a more typical agent, you will end up [here](http://a.app.qq.com/o/simple.jsp?pkgname=com.ledble), which will offer up an APK from [a qq.com domain](http://imtt.dd.qq.com/16891/channel_70005544_991653_1534577641046.apk?hsr=5848&fsname=YYB.998886.84d8e7444a0ae3d9cce8012e2cb23c68.991653.apk). Perhaps it’s a network of redirects through to an app-store that offers up different files depending on what device appears to be requesting the app.  


Regardless, anything that requires a user install an app from an un-known 3rd party should [make](https://venturebeat.com/2018/03/15/google-60-3-of-potentially-harmful-android-apps-in-2017-were-detected-via-machine-learning/) [you](https://www.makeuseof.com/tag/avoid-dangerous-apps-android/) [very](https://www.wandera.com/third-party-app-stores/) [nervous](https://blog.appknox.com/the-hidden-dangers-of-using-a-third-party-mobile-app/) 😰.



```
$ curl -L -vvv http://www.hao2b.cn/69605
*   Trying 121.41.78.187...
* TCP_NODELAY set
* Connected to www.hao2b.cn (121.41.78.187) port 80 (#0)
> GET /69605 HTTP/1.1
> Host: www.hao2b.cn
> User-Agent: curl/7.54.0
> Accept: */*
>
< HTTP/1.1 302 Found
< Server: nginx/1.10.0 (Ubuntu)
< Date: Sat, 18 Aug 2018 23:14:33 GMT
< Content-Type: text/html;charset=utf-8
< Transfer-Encoding: chunked
< Connection: keep-alive
< Location: http://2bai.co/69605
<
* Ignoring the response-body
* Connection #0 to host www.hao2b.cn left intact
* Issue another request to this URL: 'http://2bai.co/69605'
*   Trying 47.74.36.97...
* TCP_NODELAY set
* Connected to 2bai.co (47.74.36.97) port 80 (#1)
> GET /69605 HTTP/1.1
> Host: 2bai.co
> User-Agent: curl/7.54.0
> Accept: */*
>
< HTTP/1.1 301 Moved Permanently
< Server: nginx/1.12.2
< Date: Sat, 18 Aug 2018 23:14:38 GMT
< Content-Type: text/html
< Content-Length: 185
< Connection: keep-alive
< Location: http://qr.hotlnk.cn/69605
<
* Ignoring the response-body
* Connection #1 to host 2bai.co left intact
* Issue another request to this URL: 'http://qr.hotlnk.cn/69605'
*   Trying 58.218.215.159...
* TCP_NODELAY set
* Connected to qr.hotlnk.cn (58.218.215.159) port 80 (#2)
> GET /69605 HTTP/1.1
> Host: qr.hotlnk.cn
> User-Agent: curl/7.54.0
> Accept: */*
>
< HTTP/1.1 301 Moved Permanently
< Server: Tengine
< Content-Type: text/html
< Content-Length: 194
< Connection: keep-alive
< Date: Sat, 18 Aug 2018 23:14:44 GMT
< Location: http://link.weigongju.org/69605
< Via: cache43.l2et15-2[10,301-0,M], cache12.l2et15-2[13,0], kunlun8.cn192[78,301-0,M], kunlun9.cn192[79,0]
< X-Cache: MISS TCP_MISS dirn:-2:-2
< X-Swift-SaveTime: Sat, 18 Aug 2018 23:14:44 GMT
< X-Swift-CacheTime: 0
< Timing-Allow-Origin: *
< EagleId: 3adad78915346340841463468e
<
* Ignoring the response-body
* Connection #2 to host qr.hotlnk.cn left intact
* Issue another request to this URL: 'http://link.weigongju.org/69605'
*   Trying 66.102.255.30...
* TCP_NODELAY set
* Connected to link.weigongju.org (66.102.255.30) port 80 (#3)
> GET /69605 HTTP/1.1
> Host: link.weigongju.org
> User-Agent: curl/7.54.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Server: Tengine
< Content-Type: text/html;charset=utf-8
< Transfer-Encoding: chunked
< Connection: keep-alive
< Date: Sat, 18 Aug 2018 23:14:51 GMT
< Via: cache3.l2hk1[116,200-0,M], cache4.l2hk1[117,0], cache5.us1[1038,200-0,M], cache2.us1[1040,0]
< X-Cache: MISS TCP_MISS dirn:-2:-2 mlen:-1
< X-Swift-SaveTime: Sat, 18 Aug 2018 23:14:51 GMT
< X-Swift-CacheTime: 0
< Timing-Allow-Origin: *
< EagleId: 4266ff0215346340904952768e
<
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta name="language" content="zh_CN">
	<meta name="description" content="LEDBLE" />
    <meta name="keywords" content="LedBle Bluetooth 4.0 is an intelligent LED control software, which supports RGBW lighting control. It can control the light color, brightness, etc. as well as model; it has a color control, color temperature control, color control, music control and timing functions; which has a built-mode, DIY mode, custom mode, microphone and so on.<br /><br />Use tutorial videos：<br />http://v.youku.com/v_show/id_XMTQ0MTU5MDQ5Mg==.html" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimum-scale=1.0, maximum-scale=1.0">
    <meta content="email=no" name="format-detection">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta name="format-detection" content="telephone=no">
	<link rel="shortcut icon" type="image/x-icon" href="/static/scan/images/favicon.ico?v=20170320">
	<title>LEDBLE</title>
</head>

<body>
    <script type="text/javascript">
        window.location.href = "https://play.google.com/store/apps/details?id=com.ledble";
    </script>
</body>
* Connection #3 to host link.weigongju.org left intact
```

Perhaps not that surprisingly, the Google Play APK and the APK at the end of the redirect-rainbow are not the same!


```
$ ll -lah *.apk

-rw-r--r--@ 1 karl  users   8.3M Aug 18 16:20 YYB.998886.84d8e7444a0ae3d9cce8012e2cb23c68.991653.apk
-rw-r--r--  1 karl  users   6.8M Jul 31 12:36 google-play.apk

$ shasum *.apk
44b81a13a26b85fcc44c8b13c13fd6edc7e6d781  YYB.998886.84d8e7444a0ae3d9cce8012e2cb23c68.991653.apk
adc14597f416354832edafe39ad5dc55d3ca4283  google-play.apk
```


While there are some curious differences between the APK from Google Play and the random App Store, I only  dared to install the APK from the [Google Play Store](https://play.google.com/store/apps/details?id=com.ledble).

I have included both versions of the APK that I was working with at the time of this project.

- [`google-play.apk (adc14597f416354832edafe39ad5dc55d3ca4283)`](google-play.apk)

- [`YYB.998886.84d8e7444a0ae3d9cce8012e2cb23c68.991653.apk (44b81a13a26b85fcc44c8b13c13fd6edc7e6d781)`](YYB.998886.84d8e7444a0ae3d9cce8012e2cb23c68.991653.apk)
