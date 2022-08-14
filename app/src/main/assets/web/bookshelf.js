﻿var $ = document.querySelector.bind(document)
    , $$ = document.querySelectorAll.bind(document)
    , $c = document.createElement.bind(document)
    , randomImg = "http://api.mtyqx.cn/api/random.php"
    , randomImg2 = "http://img.xjh.me/random_img.php"
    , books
    ;

var now_chapter = -1;
var sum_chapter = 0;

var formatTime = value => {
    return new Date(value).toLocaleString('zh-CN', {
        hour12: false, year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit", second: "2-digit"
    }).replace(/\//g, "-");
};

var apiMap = {
    "getBookshelf": "/getBookshelf",
    "getChapterList": "/getChapterList",
    "getBookContent": "/getBookContent",
    "saveBook": "/saveBook"
};

var apiAddress = (apiName, url) => {
    let address = $('#address').value || window.location.host;
    if (!(/^http|^\/\//).test(address)) {
        address = "//" + address;
    }
    if (!(/:\d{4,}/).test(address.split("//")[1].split("/")[0])) {
        address += ":1122";
    }
    localStorage.setItem('address', address);
    return address + apiMap[apiName] + (url ? "?url=" + encodeURIComponent(url) : "");
};

var init = () => {
    $('#allcontent').classList.remove("read");
    $('#books').innerHTML = "";
    fetch(apiAddress("getBookshelf"), { mode: "cors" })
        .then(res => res.json())
        .then(data => {
            if (!data.isSuccess) {
                alert(getBookshelf.errorMsg);
                return;
            }
            books = data.data.sort((book1, book2) => book1.serialNumber - book2.serialNumber);
            books.forEach((book, i) => {
                let bookDiv = $c("div");
                let img = $c("img");
                img.src = book.bookInfoBean.coverUrl || randomImg;
                img.setAttribute("data-series-num", i);
                bookDiv.appendChild(img);
                bookDiv.innerHTML += `<table><tbody>
                                <tr><td>书名：</td><td>${book.bookInfoBean.name}</td></tr>
                                <tr><td>作者：</td><td>${book.bookInfoBean.author}</td></tr>
                                <tr><td>阅读：</td><td>${book.durChapterName}<br>${formatTime(book.finalDate)}</td></tr>
                                <tr><td>更新：</td><td>${book.lastChapterName}<br>${formatTime(book.finalRefreshData)}</td></tr>
                                <tr><td>来源：</td><td>${book.bookInfoBean.origin}</td></tr>
                                </tbody></table>`;
                $('#books').appendChild(bookDiv);
            });
            $$('#books img').forEach(bookImg =>
                bookImg.addEventListener("click", () => {
                    now_chapter = -1;
                    sum_chapter = 0;
                    $('#allcontent').classList.add("read");
                    var book = books[bookImg.getAttribute("data-series-num")];
                    $("#info").innerHTML = `<img src="${bookImg.src}">
                                        <p>　　来源：${book.bookInfoBean.origin}</p>
                                        <p>　　书名：${book.bookInfoBean.name}</p>
                                        <p>　　作者：${book.bookInfoBean.author}</p>
                                        <p>阅读章节：${book.durChapterName}</p>
                                        <p>阅读时间：${formatTime(book.finalDate)}</p>
                                        <p>最新章节：${book.lastChapterName}</p>
                                        <p>检查时间：${formatTime(book.finalRefreshData)}</p>
                                        <p>　　简介：${book.bookInfoBean.introduce.trim().replace(/\n/g, "<br>")}</p>`;
                    window.location.hash = "";
                    window.location.hash = "#info";
                    $("#content").innerHTML = "章节列表加载中...";
                    $("#chapter").innerHTML = "";
                    fetch(apiAddress("getChapterList", book.noteUrl), { mode: "cors" })
                        .then(res => res.json())
                        .then(data => {
                            if (!data.isSuccess) {
                                alert(data.errorMsg);
                                $("#content").innerHTML = "章节列表加载失败！";
                                return;
                            }

                            data.data.forEach(chapter => {
                                let ch = $c("button");
                                ch.setAttribute("data-url", chapter.durChapterUrl);
                                ch.setAttribute("data-index", chapter.durChapterIndex);
                                ch.setAttribute("title", chapter.durChapterName);
                                ch.innerHTML = chapter.durChapterName.length > 15 ? chapter.durChapterName.substring(0, 14) + "..." : chapter.durChapterName;
                                $("#chapter").appendChild(ch);
                            });
                            sum_chapter = data.data.length;
                            $('#chapter').scrollTop = 0;
                            $("#content").innerHTML = "章节列表加载完成！";
                        });

                }));
        });
};

$("#back").addEventListener("click", () => {
    if (window.location.hash === "#content") {
        window.location.hash = "#chapter";
    } else if (window.location.hash === "#chapter") {
        window.location.hash = "#info";
    } else {
        $('#allcontent').classList.remove("read");
    }
});

$("#refresh").addEventListener("click", init);

$('#hidebooks').addEventListener("click", () => {
    $("#books").classList.toggle("hide");
    $(".nav").classList.toggle("hide");
    $("#allcontent").classList.toggle("allscreen");
});

$('#top').addEventListener("click", () => {
    window.location.hash = "";
    window.location.hash = "#info";
});

$('#showchapter').addEventListener("click", () => {
    window.location.hash = "";
    window.location.hash = "#chapter";
});

$('#up').addEventListener('click', e => {
    if (now_chapter > 0) {
        now_chapter--;
        let clickEvent = document.createEvent('MouseEvents');
        clickEvent.initEvent("click", true, false);
        $('[data-index="' + now_chapter + '"]').dispatchEvent(clickEvent);
    } else if (now_chapter == 0) {
        alert("已经是第一章了^_^!")
    } else {

    }
});

$('#down').addEventListener('click', e => {
    if (now_chapter > -1) {
        if (now_chapter < sum_chapter - 1) {
            now_chapter++;
            let clickEvent = document.createEvent('MouseEvents');
            clickEvent.initEvent("click", true, false);
            $('[data-index="' + now_chapter + '"]').dispatchEvent(clickEvent);

        } else {
            alert("已经是最后一章了^_^!")
        }
    }
});

$('#chapter').addEventListener("click", (e) => {
    if (e.target.tagName === "BUTTON") {
        var url = e.target.getAttribute("data-url");
        var index = e.target.getAttribute("data-index");
        var name = e.target.getAttribute("title");
        if (!url) {
            alert("未取得章节地址");
        }
        now_chapter = parseInt(index);
        $("#content").innerHTML = "<p>" + name + " 加载中...</p>";
        fetch(apiAddress("getBookContent", url), { mode: "cors" })
            .then(res => res.json())
            .then(data => {
                if (!data.isSuccess) {
                    alert(data.errorMsg);
                    $("#content").innerHTML = "<p>" + name + " 加载失败！</p>";
                    return;
                }
                var content = data.data.trim().split("\n\n");
                if (content.length === 2) {
                    $("#content").innerHTML = `<h2>${content[0]}</h2>　　（全文 ${content[1].length} 字）<br><br>　　` + content[1].trim().replace(/\n/g, "<br><br>");
                } else {
                    $("#content").innerHTML = `<h2>${name || e.target.innerHTML}</h2>　　（全文 ${data.data.length} 字）<br><br>　　` + data.data.trim().replace(/\n/g, "<br><br>");
                }
                window.location.hash = "";
                window.location.hash = "#content";
            });
    }
});

$('#address').setAttribute("placeholder", "阅读APP地址或IP：" + window.location.host);
if (!$('#address').value && typeof localStorage && localStorage.getItem('address')) {
    $('#address').value = localStorage.getItem('address');
}
init();
