function letterstobg() {

  var cv_menu = document.getElementById("cv_menu");
  console.log("letterstobg");
  if (cv_menu !== null) {
    var ot_randomletters = document.getElementById("pageform:ot_randomletters");
    var lettersstr = ot_randomletters.innerHTML;
    var letters = lettersstr.split(".");
    var captions = [
      "", "", "A", "", "J", "Á", "T", "É", "K", "",
      "", "S", "Z", "A", "B", "Á", "LY", "A", "I", "",
      "", "Ú", "J", "", "J", "Á", "T", "SZ", "M", "A",
      "", "I", "N", "D", "Í", "T", "Á", "S", "A", "",
      "", "", "J", "Á", "T", "SZ", "M", "A", "", "",
      "", "F", "O", "LY", "T", "A", "T", "Á", "S", "A",
      "", "", "I", "N", "D", "U", "L", "Ó", "", "",
      "", "J", "Á", "T", "SZ", "M", "Á", "K", "", "",
      "", "L", "E", "G", "U", "T", "Ó", "B", "B", "I",
      "E", "R", "E", "D", "M", "É", "N", "Y", "E", "K",
      "", "", "J", "Á", "T", "SZ", "M", "A", "", "",
      "", "B", "E", "T", "Ö", "L", "T", "É", "S", "E",
      "", "", "A", "", "J", "Á", "T", "É", "K", "",
      "", "", "N", "É", "V", "J", "E", "GY", "E", ""];

    cv_menu.width = 312;
    cv_menu.height = 434;

    var ctx = cv_menu.getContext("2d");
    ctx.font = "bold 20px Itim";
    ctx.fillStyle = "rgba(128,133,113,0.25)";
    ctx.textAlign = "center";

    for (var row = 0; row < 14; row++) {
      let wstart = -1;
      let wlength = 0;
      for (var col = 0; col < 10; col++) {
        let index = row * 10 + col;
        let letter;
        if (captions[index] !== "") {
          letter = captions[index];
          ctx.fillStyle = "#707561";
          if (wstart < 0)
            wstart = col;
        } else {
          letter = letters[index];
          ctx.fillStyle = "rgba(128,133,113,0.25)";
          if (wstart >= 0) {
            wlength = col - wstart;
          }
        }
        if (col === 9 && wstart >= 0 && wlength === 0) {
          wlength = 10 - wstart;
        }

        /* 2. paraméter: horizontális, 3. paraméter: vertikális pozíció */
        ctx.fillText(letter, 31 * col + 17, 31 * row + 23, 40);

        if (wlength > 0) {
          drawWordRect(ctx, row, wstart, wlength, (row % 4 === 0 || row % 4 === 1 ? true : false));
          wlength = 0;
          wstart = -1;
        }
      }

    }

//    drawWordRect(ctx, 0, 1, 2, true);
//    drawWordRect(ctx, 0, 4, 6, true);
//    drawWordRect(ctx, 1, 1, 8, true);
//
//    drawWordRect(ctx, 2, 2, 6, false);
//    drawWordRect(ctx, 3, 1, 9, false);
//    
//    drawWordRect(ctx, 4, 2, 6, true);
//    drawWordRect(ctx, 5, 1, 7, true);
//
//    drawWordRect(ctx, 6, 2, 7, false);
//    drawWordRect(ctx, 7, 1, 9, false);
//    
//    drawWordRect(ctx, 8, 1, 1, true);
//    drawWordRect(ctx, 8, 3, 5, true);
//    drawWordRect(ctx, 9, 1, 7, true);
//
//    drawWordRect(ctx, 10, 3, 6, false);
//    drawWordRect(ctx, 11, 1, 7, false);
//
//    drawWordRect(ctx, 12, 2, 7, true);
//    drawWordRect(ctx, 13, 0, 1, true);
//    drawWordRect(ctx, 13, 2, 8, true);

  }

}

function drawWordRect(ctx, line, startpos, length, altcolor) {
  const cornerx = 2;
  const cornery = 2;

  var startx = cornerx + 31 * startpos;
  var starty = cornerx + 31 * line;
  var widthx = 31 * length - 4;
  var widthy = 23;

  ctx.beginPath();
  ctx.lineWidth = 1.5;

  starty = starty + 2;
  ctx.setLineDash([4, 2]);

  if (altcolor) {
    ctx.strokeStyle = "rgb(108, 168, 83)";
  } else {
    ctx.strokeStyle = "rgb(219, 50, 50)";
  }

  ctx.rect(startx, starty, widthx, widthy);

  ctx.stroke();
}

function getfilename() {
  var if_loadgame = document.getElementById("pageform:if_loadgame");
  if (if_loadgame !== null) {
    var in_filename = document.getElementById("in_filename");
    if (in_filename !== null) {
      if_loadgame.onchange = function () {
        if (this.files[0]) {
          in_filename.value = this.files[0].name;
        } else {
          in_filename.value = "";
        }
      };
    }
  }
}

function switchFullScreen() {
  var isInFullScreen = (document.fullscreenElement && document.fullscreenElement !== null) ||
          (document.webkitFullscreenElement && document.webkitFullscreenElement !== null) ||
          (document.mozFullScreenElement && document.mozFullScreenElement !== null) ||
          (document.msFullscreenElement && document.msFullscreenElement !== null);

  var docElm = document.documentElement;
  var a_fullscreen = document.getElementById("a_fullscreen");
  
  console.log(a_fullscreen === null);
  if (a_fullscreen !== null)
    console.log(a_fullscreen.innerHTML);
  
  if (!isInFullScreen) {
    if (docElm.requestFullscreen) {
      docElm.requestFullscreen();
    } else if (docElm.mozRequestFullScreen) {
      docElm.mozRequestFullScreen();
    } else if (docElm.webkitRequestFullScreen) {
      docElm.webkitRequestFullScreen();
    } else if (docElm.msRequestFullscreen) {
      docElm.msRequestFullscreen();
    }
    a_fullscreen.innerHTML = "Kilépés a teljes képernyőből";
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen();
    } else if (document.mozCancelFullScreen) {
      document.mozCancelFullScreen();
    } else if (document.msExitFullscreen) {
      document.msExitFullscreen();
    }
    a_fullscreen.innerHTML = "Váltás teljes képernyőre";
  }
}

function addFSEventListener() {
  document.addEventListener('fullscreenchange', exitHandler);
  document.addEventListener('webkitfullscreenchange', exitHandler);
  document.addEventListener('mozfullscreenchange', exitHandler);
  document.addEventListener('MSFullscreenChange', exitHandler);
}

function exitHandler() {
  var isInFullScreen = (document.fullscreenElement && document.fullscreenElement !== null) ||
          (document.webkitFullscreenElement && document.webkitFullscreenElement !== null) ||
          (document.mozFullScreenElement && document.mozFullScreenElement !== null) ||
          (document.msFullscreenElement && document.msFullscreenElement !== null);

  var a_fullscreen = document.getElementById("a_fullscreen");
  if (!isInFullScreen) {
    a_fullscreen.innerHTML = "Váltás teljes képernyőre";
  } else {
    a_fullscreen.innerHTML = "Kilépés a teljes képernyőből";
  }
}    

function playSound(num){
  console.log(num);
  var stopTick = false;
  var chkTick = false;
  
  switch(num) {
  case 1: au_elem = document.getElementById('au_btnlogin'); break;
  case 2: au_elem = document.getElementById('au_btnlogout'); break;
  case 3: au_elem = document.getElementById('au_btnjoin'); break;
  case 4: au_elem = document.getElementById('au_btnmenu'); break;
  case 5: au_elem = document.getElementById('au_btnback'); break;
  case 6: au_elem = document.getElementById('au_btnother'); break;
  case 20: au_elem = document.getElementById('au_letter'); stopTick = true; break;
  case 21: au_elem = document.getElementById('au_letter'); break;
  case 22: au_elem = document.getElementById('au_letterback'); break;
  case 23: au_elem = document.getElementById('au_letterok'); stopTick = true; break;
  case 25: au_elem = document.getElementById('au_board'); chkTick = true; break;
  case 26: au_elem = document.getElementById('au_board1'); break;
  case 27: au_elem = document.getElementById('au_board2'); chkTick = true; break;
  case 28: au_elem = document.getElementById('au_timeover'); stopTick = true; break;
  case 29: au_elem = document.getElementById('au_tick'); break;
  case 30: au_elem = document.getElementById('au_checkbox'); break;
  case 31: au_elem = document.getElementById('au_combo1'); break;
  case 32: au_elem = document.getElementById('au_combo2'); break;
  case 33: au_elem = document.getElementById('au_btnsub'); break;
  case 34: au_elem = document.getElementById('au_btnadd'); break;
  case 37: au_elem = document.getElementById('au_btnopen'); break;
  case 38: au_elem = document.getElementById('au_btnstart'); break;
  case 39: au_elem = document.getElementById('au_btndrop'); break;
  case 40: au_elem = document.getElementById('au_start'); break;
  case 41: au_elem = document.getElementById('au_newround'); break;
  case 70: au_elem = document.getElementById('au_wordok'); break;
  case 71: au_elem = document.getElementById('au_wordnok'); break;
  case 72: au_elem = document.getElementById('au_btnquit'); stopTick = true; break;
  case 73: au_elem = document.getElementById('au_win'); stopTick = true; break;
  case 74: au_elem = document.getElementById('au_lose'); stopTick = true; break;
  }   
  console.log(au_elem === null);

  if (chkTick){
    var ot_gamehist = document.getElementById("pageform:ot_gamehist");
    if (ot_gamehist !== null) {
      if (ot_gamehist.innerHTML.length > 1)
        if ((ot_gamehist.innerHTML).charAt(ot_gamehist.innerHTML.length - 2) === "0")
          stopTick = true;
    }
  }

  if (stopTick){
    stopSound(29);
  }  
  
  if (au_elem !== null){
    au_elem.volume = 0.4;
    au_elem.play();
  }
}

function stopSound(num){
  console.log(num);
  
  switch(num) {
  case 29: au_stop = document.getElementById('au_tick'); break;
  }   
  console.log(au_stop === null);
  if (au_stop !== null){
    au_stop.pause();
    au_stop.currentTime = 0;    
  }
}

function checkwordSound(){
  console.log("checkwordSound");
  im_wordchk = document.getElementById('pageform:im_wordok');
  if (im_wordchk !== null){
    playSound(70);
  } else {
    im_wordchk = document.getElementById('pageform:im_wordnok');
    if (im_wordchk !== null){
      playSound(71);
    }
  }
}

function boardBtnClick(el){
  if (el !== null){
    var tick = '\u2713';
    if (el.innerHTML.includes(tick))
      playSound(27);
    else
      playSound(26);
  }  
}

function hideTooltips(){
  var elems = document.getElementsByClassName("tooltiptext");
  for(var i = 0; i < elems.length; i++){
    elems[i].style.display = "none";
  }
}
