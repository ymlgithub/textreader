# textreader
为文盲、视障人群朗读文本。

## 下载
百度OCR服务每天的免费配额有限，因此不提供apk文件下载，有需要的请自行申请百度OCR服务授权文件并编译。

## 使用说明
<p>
<img src='imgs/a.jpg' width='30%' style="display:inline;"/>
<img src='imgs/b.jpg' width='30%' style="display:inline;"/>
<img src='imgs/c.jpg' width='30%' style="display:inline;"/>
</p>

## for 开发者

OCR部分采用[百度OCR服务](https://ai.baidu.com/ai-doc/OCR/ok3h7xx8c)，使用授权文件获得AccessToken。
1. 在[官网](https://console.bce.baidu.com/ai/?fromai=1&_=1488766023093#/ai/ocr/app/list)中配置应用  
![](https://ai.bdstatic.com/file/E0FE42DB27494CBC895C6F24DBC1FE54)
![](https://ai.bdstatic.com/file/36B5703778884B73AE6E9241730B1772)
2. 在***应用详情***页面下载对应应用的授权文件  
![](https://ai.bdstatic.com/file/6E928A2EBAE744E59D8D0CE2984AAC57)
3. 将授权文件添加至工程assets文件夹，文件名必须为`aip.license`  
![](https://ai.bdstatic.com/file/54D522AC76AA44B9BBE6E98FEEAD79EE)
