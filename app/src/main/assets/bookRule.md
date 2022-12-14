# 书源规则说明

## 目录
* 1、语法
* 2、基本
* 3、搜索
* 4、发现
* 5、详情页
* 6、目录
* 7、正文
* 8、示例

## 语法
* JSOUP
```
*                           通用元素选择器，匹配任何元素
E                           标签选择器，匹配所有使用E标签的元素
.info                       class选择器，匹配所有class属性中包含info的元素
#footer                     id选择器，匹配所有id属性等于 footer 的元素
E,F                         多元素选择器，同时匹配所有E元素或F元素，E和F之间用逗号分隔
EF                          后代元素选择器，匹配所有属于E元素后代的F元素，E和F之间用空格分隔
E>F                         子元素选择器，匹配所有E元素的子元素F
E+F                         毗邻元素选择器，匹配紧随E元素之后的同级元素F（只匹配第一个）
E～F                        同级元素选择器，匹配所有在E元素之后的同级F元素
E[atte'val']                属性 att 的值为 val的E元素（区分大小写
E[att='val'                 属性 att 的值以val开头的E元素（区分大小写
E[atts='val'                属性 att 的值以 val结尾的E元素（区分大小写
E[att*='val'}               属性 att 的值包含val的E元素（区分大小写
E[attl='vl'][att2*='v2']    属性 attl的值为vl，att2的值包含v2（区分大小写
E:contains('xXXx'           内容中包含XXXx的E元素
E:not(s）                   匹配不符合当前选择器的任何元素

注意：必须以 @css: 开头
```

* JSONPath
```
符号	描述
$                       查询的根节点对象，用于表示一个json数据，可以是数组或对象
@                       过滤器断言（filter predicate）处理的当前节点对象，类似于java中的this字段
*                       通配符，可以表示一个名字或数字
..                      可以理解为递归搜索，Deep scan. Available anywhere a name is required.
.<name>	                表示一个子节点
[‘<name>’ (, ‘<name>’)] 表示一个或多个子节点
[<number> (, <number>)] 表示一个或多个数组下标
[start:end]             组片段，区间为[start,end),不包含end
[?(<expression>)]       过滤器表达式，表达式结果必须是boolean
min()	                获取数值类型数组的最小值，输出Double类型数据
max()	                获取数值类型数组的最大值，输出Double类型数据
avg()	                获取数值类型数组的平均值，输出Double类型数据
stddev()                获取数值类型数组的标准差，输出Double类型数据
length()                获取数值类型数组的长度，输出Integer类型数据
==                      等于符号，但数字1不等于字符1(note that 1 is not equal to ‘1’)
!=                      不等于符号
<                       小于符号
<=                      小于等于符号
>                       大于符号
>=                      大于等于符号
=~                      判断是否符合正则表达式，如[?(@.name =~ /foo.*?/i)]
in                      所属符号，如[?(@.size in [‘S’, ‘M’])]
nin                     排除符号
size                    获取数据数目
empty                   判空符号

注意：最好以 @json: 或 $. 开头，其他形式不可靠
```

* 正则表达式
```
.           匹配除换行符以外的所有字符
d,s,w       分别匹配数字、空格和制表符、字母数字字符和“_”
D,S,W       分别匹配非数字、非空格、非制表符、非字母数字字符
^           匹配文本的开头
$           匹配文本的结尾
[abc]	    匹配以下三个元素之一：a、b或c
[a-c]	    匹配一个按a到c顺序排列的元素
[^abc]	    将不相等的元素与以下三个元素中的任何一个匹配
aa|cc	    匹配aa或cc
?           问号表示前面的元素出现零次或一次。
\*          星号表示前面的元素出现的次数为零或多次。
\+   	    加号表示前面的元素出现一次或多次。
{n}         前面的项正好匹配了n次。
{min,}	    前面的项匹配了min次或更多次。
{,max}	    前面的项目最多匹配max次。
{min,max}   前面的项至少匹配了min次，但不超过max次。
( )         定义标记的子表达式。括号内匹配的字符串可在以后重新调用(请参阅下一条目， )。标记的子表达式也称为块或捕捉组。您可以\1使用它。
(?:expr)    忽略此标记的子表达式
(?=expr)    正向预查(Positive Lookahead)
(?!expr)    负向预查(Negative Lookahead)
&&          合并所有取到的值
||          以第一个取到值的为准
%%          依次取数
```

## 基本
* 1、书源URL(bookSourceUrl)
  + 必填
  + 唯一标识，不可重复
  + 与其他源相同会覆盖
* 2、书源名称(bookSourceName)
  + 必填
  + 名字可重复
* 3、书源分组(bookSourceGroup)
  + 可不填
  + 用于整理源
* 4、登录URL(loginUrl)
  + 根据需求，随机应变
  + 用于登录个人账户
* 5、登陆UI(loginUi)
* 6、登陆检查(loginCheckJS)

## 搜索
* 1、搜索地址(ruleSearchUrl)
  + key为关键字标识，通常形态为`{{key}}`，运行时会替换为搜索关键字
  + 也可以对key进行加密等操作，如：`{{java.base64Encode(key)}}`
  + page为关键字标识，通常形态为`{{page}}`
  + 支持相对URL
* 2、搜索列表规则(ruleSearchList)
* 3、搜索书名规则(ruleSearchName)
* 4、搜索作者规则(ruleSearchAuthor)
* 5、搜索分类规则(ruleSearchKind)
* 6、搜索最新章节规则(ruleSearchLastChapter)
* 7、搜索简介规则(ruleSearchIntroduce)
* 8、搜索封面规则(ruleSearchCoverUrl)
* 9、搜索书籍url规则(ruleSearchNoteUrl)

## 发现
* 1、发现规则(ruleFindUrl)
  + page为关键字标识，通常形态为`{{page}}`，page的初值为1，也可以对page进行计算，
  + 如：`{{(page-1)*20}}`，有时会遇到第一页没有页数的情况，有两种方法：
  + ① `{{page - 1 == 0 ? "": page}}`
  + ② `<,{{page}}>`
  + 格式一，如：`名称::http://www.baidu.com`，发现URL可使用&&或换行符\n隔开
  + 格式二，有5个样式属性(layout_flexGrow、layout_flexShrink、layout_alignSelf、layout_flexBasisPercent、layout_wrapBefore)
  + 支持相对URL
* 2、发现列表规则(ruleFindList)
* 3、发现书名规则(ruleFindName)
* 4、发现作者规则(ruleFindAuthor)
* 5、发现分类规则(ruleFindKind)
* 6、发现简介规则(ruleFindIntroduce)
* 7、发现最新章节规则(ruleFindLastChapter)
* 8、发现封面规则(ruleFindCoverUrl)
* 9、发现书籍url规则(ruleFindNoteUrl)

## 详情
* 1、书籍详情URL正则(ruleBookUrlPattern)
* 2、书籍详情预处理规则(ruleBookInfoInit)
  + 只能使用正则表达式或者js
  + js的返回值需要是json对象
* 3、书名规则(ruleBookName)
* 4、作者规则(ruleBookAuthor)
* 5、封面规则(ruleCoverUrl)
* 6、简介规则(ruleBookIntroduce)
* 7、分类规则(ruleBookKind)
* 8、最新章节规则(ruleBookLastChapter)
* 9、目录URL规则(ruleChapterUrl)
  + 只支持单个url
  + 允许修改书名作者(canReName)
  + 规则不为空且详情页书名不为空，使用详情页中的作者。否则，使用搜索页中的书名
  + 规则不为空且详情页作者不为空，使用详情页中的作者。否则，使用搜索页中的作者

## 目录
* 1、目录下一页规则(ruleChapterUrlNext)
  + 支持单个url
  + 支持url数组
  + js中返回`[]``null``""`时停止加载下一页
* 2、目录列表规则(ruleChapterList)
  + 首字符使用负号(-)可使列表反序
* 3、章节名称规则(ruleChapterName)
* 4、章节URL规则(ruleChapterUrl)
* 5、VIP标识(ruleChapterVip)
* 6、购买标志(ruleChapterPay)
  + 当结果为`null``false``0``""`时为非VIP

## 正文
* 1、正文下一页URL规则(ruleContentUrlNext)
  + 支持单个url
  + 支持url数组
* 2、正文规则(ruleBookContent)
  + 正文图片链接支持修改headers
  + book对象的可用属性
  + 使用方法: 在js中或`{{}}`中使用`book.属性`的方式即可获取，如在正文内容后加上`##{{book.name+"正文卷"+title}}`可以净化 书名+正文卷+章节名称（如：我是大明星正文卷第二章我爸是豪门总裁）这一类的字符
  + chapter对象的可用属性
  + 使用方法: 在js中或`{{}}`中使用`chapter.属性`的方式即可获取，如在正文内容后加上`##{{chapter.title+chapter.index}}`可以净化 章节标题+序号(如 第二章 天仙下凡2) 这一类的字符
* 3、正文替换规则(ruleBookContentReplace)
* 4、HttpUserAgent

## 示例
```Json
{
    "bookSourceUrl": "https://www.kaixin7days.com",
    "bookSourceName": "消消乐听书",
    "bookSourceGroup": "听书",
    "bookSourceType": "AUDIO",
    "loginUrl": "var loginInfo = source.getLoginInfo()\nvar json = java.getResponse(\"https://www.kaixin7days.com/login@\" + loginInfo).body()\nvar loginRes = JSON.parse(json)\nvar header = null\nif (loginRes.statusCode == 200) {\n    var accessToken = {\n        Authorization: \"Bearer \" + loginRes.content.accessToken\n    }\n    header = JSON.stringify(accessToken)\n    source.putLoginHeader(header)\n}\nheader",
    "loginUi": "[{\"name\": \"telephone\",\"type\": \"text\"},{\"name\": \"password\",\"type\": \"password\"},{\"type\": \"button\",\"name\": \"注册\", \"action\": \"http://www.yooike.com/xiaoshuo/#/register?title=%E6%B3%A8%E5%86%8C\"}]",
    "loginCheckJs": "var strRes = result\nvar c = JSON.parse(result.body())\nif (c.statusCode == 301) {\n    var loginInfo = source.getLoginInfo()\n    var dl = null\n    if (loginInfo) {\n        dl = java.getResponse(\"https://www.kaixin7days.com/login@\" + loginInfo).body()\n    } else {\n        dl = java.getResponse('https://www.kaixin7days.com/visitorLogin@{\"deviceId\":\"'+java.androidId()+'\"}').body()\n    }\n    c = JSON.parse(dl)\n    var accessToken = {\n        Authorization: \"Bearer \" + c.content.accessToken\n    }\n    var header = JSON.stringify(accessToken)\n    source.putLoginHeader(header)\n    strRes = java.getResponse(\"@Header:\" + header + url)\n}\nstrRes",
    "serialNumber": -100,
    "enable": true,
    "ruleFindUrl": "@js:var header = source.getLoginHeader()\nvar json = \"\"\nvar j = null\nif (header != null) {\n    json = java.getResponse(\"@Header:\" + header + \"https://www.kaixin7days.com/book-service/bookMgt/getBookCategroy@{}\").body()\n    j = JSON.parse(json)\n}\nif (j == null || j.statusCode != 200) {\n    json = java.getResponse(\"https://www.kaixin7days.com/visitorLogin@{}\").body()\n    j = JSON.parse(json)\n    var accessToken = {\n        Authorization: \"Bearer \" + j.content.accessToken\n    }\n    header = JSON.stringify(accessToken)\n    source.putLoginHeader(header)\n    json = java.getResponse(\"@Header:\" + header + \"https://www.kaixin7days.com/book-service/bookMgt/getBookCategroy@{}\").body()\n    j = JSON.parse(json)\n}\nvar fls = j.content\nvar fx = \"\"\nfor (var i = 0; i < fls.length; i++) {\n    fx = fx + fls[i].categoryName + '::/book-service/bookMgt/getAllBookByCategroyId@{\"categoryIds\": \"' + fls[i].associationCategoryIDs + '\",\"pageNum\": {{searchPage}},\"pageSize\": 100}&&'\n}\nfx",
    "ruleFindList": "$.content.content",
    "ruleFindName": "$.title",
    "ruleFindAuthor": "$.author",
    "ruleFindKind": "",
    "ruleFindIntroduce": "$.desc",
    "ruleFindLastChapter": "$.newestChapter",
    "ruleFindCoverUrl": "$.cover@js:var cover = JSON.parse(result);'https://www.shuidi.online:9021/fileMgt/getPicture?filePath='+cover.storeFilePath",
    "ruleFindNoteUrl": "$.id@js:java.put('bookId', result);'https://www.kaixin7days.com/book-service/bookMgt/getAllChapterByBookId@{\"bookId\": \"'+result+'\",\"pageNum\": 1,\"pageSize\": 100000}'",
    "ruleSearchUrl": "https://www.kaixin7days.com/book-service/bookMgt/findBookName@{\"title\": \"searchKey\",\"pageNum\": {{searchPage}},\"pageSize\": 100}",
    "ruleSearchList": "$.content.content",
    "ruleSearchName": "$.title",
    "ruleSearchAuthor": "$.author",
    "ruleSearchIntroduce": "$.desc",
    "ruleSearchLastChapter": "$.newestChapter",
    "ruleSearchCoverUrl": "$.cover@js:var cover = JSON.parse(result);'https://www.shuidi.online:9021/fileMgt/getPicture?filePath='+cover.storeFilePath",
    "ruleSearchNoteUrl": "$.id@js:java.put('bookId', result);'https://www.kaixin7days.com/book-service/bookMgt/getAllChapterByBookId@{\"bookId\": \"'+result+'\",\"pageNum\": 1,\"pageSize\": 100000}'",
    "ruleChapterList": "$.content.content",
    "ruleChapterName": "$.chapterTitle",
    "ruleChapterVip": "$.isFree@js:var vip = false; if (result == '0') { vip = true } vip",
    "ruleChapterPay": "$.isPay",
    "ruleContentUrl": "$.id@js:\"https://www.shuidi.online:9021/fileMgt/getAudioByChapterId?bookId=\" + java.getString(\"$.bookId\") + \"&chapterId=\" + result + \"&pageNum=1&pageSize=50&{{var header = JSON.parse(source.getLoginHeader());var reg = /&chapterId=(.*?)&/;var chapterId = reg.exec(result)[1];var keyId = '1632746188011002';var ks = java.md5Encode(keyId + chapterId + header.Authorization);'Authorization=' + header.Authorization + '&keyId=' + keyId + '&keySecret=' + ks}\" + \"}\"",
    "ruleBookContent": "",
    "payAction": "var header = JSON.parse(source.getLoginHeader())\nvar chapterUrl = chapter.getDurChapterUrl(); var reg = /&chapterId=(.*?)&/; var chapterId = reg.exec(chapterUrl)[1]\n'http://www.shuidi.online/?name='+book.getName()+'&type=2&cover=' + book.getCoverPath() + '&chapterId=' + chapterId + '&chapter=203&allNumber=' + book.getChapterListSize()+'&bookId=' + book.getVariableMap().get('bookId') + '&chapterIds=' + chapterId + '&number=' + chapter.getDurChapterIndex() + '&accessToken=' + header.Authorization.substring(7) + '#/pay'"
}
```
