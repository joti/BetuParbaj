var ticking = false;

function menuPoll(xhr, status, args) {
  var ita_prevnames = document.getElementById("pageform:ita_names");
  if (ita_prevnames !== null) {
    var ita_newnames = document.getElementById("pageform:ita_names2");

    if (ita_prevnames.value !== ita_newnames.value) {
      ita_prevnames.value = ita_newnames.value;

      btn = document.getElementById("pageform:btn_menurefresh");
      if (btn !== null) {
        btn.click();
      }
    }
  }
}

function mainPoll(xhr, status, args) {
  var scrollPos;
  var scrollHgt;
  var prevVal;
  var newVal;
  var btn;

  var ita_prevmsg = document.getElementById("pageform:ita_messages");
  if (ita_prevmsg !== null) {
    var ita_newmsg = document.getElementById("pageform:ita_messages2");

    if (ita_prevmsg.value !== ita_newmsg.value) {
      scrollPos = ita_prevmsg.scrollTop;
      scrollHgt = ita_prevmsg.scrollHeight;

      ita_prevmsg.value = ita_newmsg.value;

      ita_prevmsg.scrollTop = scrollPos + ita_prevmsg.scrollHeight - scrollHgt;
      if ($(".chatscroll").getNiceScroll() !== null) {
        $(".chatscroll").getNiceScroll().resize();
      }
    }
  }

  var ita_prevgames = document.getElementById("pageform:ita_gamesinlobby");
  if (ita_prevgames !== null) {
    var ita_newgames = document.getElementById("pageform:ita_gamesinlobby2");

    if (ita_prevgames.value !== ita_newgames.value) {
      ita_prevgames.value = ita_newgames.value;

      btn = document.getElementById("pageform:btn_lobbyrefresh");
      if (btn !== null) {
        btn.click();
      }

    }
  }

  var ot_newgamestate = document.getElementById("pageform:ot_gamestate2");
  if (ot_newgamestate !== null) {

    var ot_prevgamestate = document.getElementById("pageform:ot_gamestate");
    if (ot_prevgamestate.innerHTML !== ot_newgamestate.innerHTML) {
      prevVal = ot_prevgamestate.innerHTML;
      newVal = ot_newgamestate.innerHTML;
      ot_prevgamestate.innerHTML = newVal;

      if ((newVal === "0" && prevVal !== newVal && prevVal !== "") // vissza a lobbiba
              || (newVal === "3" && prevVal !== newVal)) // indul a játék
      {
        btn = document.getElementById("pageform:btn_mainrefresh");
        if (btn !== null) {
          btn.click();
        }
      }
    }
  }

  var ot_newgamesetup = document.getElementById("pageform:ot_gamesetup2");
  if (ot_newgamesetup !== null) {
    var needsetuprefresh = false;
    var ot_prevgamesetup = document.getElementById("pageform:ot_gamesetup");

    if (ot_prevgamesetup !== null) {
      if (ot_prevgamesetup.innerHTML !== ot_newgamesetup.innerHTML) {
        ot_prevgamesetup.innerHTML = ot_newgamesetup.innerHTML;
        needsetuprefresh = true;
      }
    }

    if (needsetuprefresh) {
      btn = document.getElementById("pageform:btn_setuprefresh");
      if (btn !== null) {
        btn.click();
      }
    }
  }

  btn = document.getElementById("pageform:btn_pagerefresh");
  if (btn !== null) {
    var needpagerefresh = false;
    var ot_newsec = document.getElementById("pageform:ot_seconds2");
    if (ot_newsec !== null) {
      var ot_prevsec = document.getElementById("pageform:ot_seconds");
      if (ot_prevsec !== null) {
        if (ot_prevsec.innerHTML !== ot_newsec.innerHTML) {
          if (parseInt(ot_prevsec.innerHTML,10) > parseInt(ot_newsec.innerHTML,10)){
            needpagerefresh = true;
          }
          ot_prevsec.innerHTML = ot_newsec.innerHTML;
        }
      }  
    }  

    if (needpagerefresh) {
      btn.click();
    }
  }

  var ot_newturnsec = document.getElementById("pageform:ot_turnsec2");
  if (ot_newturnsec !== null) {
    var ot_playerstate = document.getElementById("pageform:ot_playerstate");
    var ot_prevturnsec = document.getElementById("pageform:ot_turnsec");

    if (ot_prevturnsec !== null) {
      if (ot_prevturnsec.innerHTML !== ot_newturnsec.innerHTML) {
        var prevval = ot_prevturnsec.innerHTML;
        ot_prevturnsec.innerHTML = ot_newturnsec.innerHTML;
        if ((ot_playerstate.innerHTML < 1) || (ot_prevturnsec.innerHTML > 999)) {
          ot_prevturnsec.style.display = "none";
          ticking = false;
        } else {
          ot_prevturnsec.style.display = "block";
          if (ot_playerstate.innerHTML > 2) {
            ot_prevturnsec.style.color = "#dee4b9";
            if (ticking) {
//              console.log("STOP TICK");
              stopSound(29);
            }
          } else if (ot_prevturnsec.innerHTML < 10) {
            ot_prevturnsec.style.color = "red";
            if (prevval >= 10){
              ticking = true;
              playSound(29);
            }
          } else {
            ot_prevturnsec.style.color = "#555c23";
          }
        }
      }
    }
  }

  btn = document.getElementById("pageform:btn_playrefresh");
  var needplayrefresh = false;
  var needplayendrefresh = false;
  if (btn !== null) {
    var ot_newgamehist = document.getElementById("pageform:ot_gamehist2");
    var ot_prevgamehist = document.getElementById("pageform:ot_gamehist");

    if (ot_prevgamehist !== null) {
      if (ot_prevgamehist.innerHTML !== ot_newgamehist.innerHTML) {
        var last = "";
        var newlast = "";
        if (ot_prevgamehist.innerHTML.length > 0)
          last = (ot_prevgamehist.innerHTML).charAt(ot_prevgamehist.innerHTML.length-1);
        
        var pos = ot_prevgamehist.innerHTML.indexOf("::");
        if (pos > -1){
          var prevturn = ot_prevgamehist.innerHTML.substring(pos + 2, pos + 4);
          pos = ot_newgamehist.innerHTML.indexOf("::");
          var newturn = ot_newgamehist.innerHTML.substring(pos + 2, pos + 4);
//          console.log("turn " + prevturn + " -> " + newturn);
          if (newturn !== prevturn){
            playSound(41);
          }
        }  
        
        ot_prevgamehist.innerHTML = ot_newgamehist.innerHTML;
        needplayrefresh = true;

        if (ot_newgamehist.innerHTML.length > 0)
          newlast = (ot_newgamehist.innerHTML).charAt(ot_newgamehist.innerHTML.length-1);
        
        if (last !== "4" && newlast === "4"){
          console.log("TIME OVER!");
          playSound(28);
        } else if (last === "" && newlast === "}"){
          console.log("START!");
          playSound(40);
        } else if (last !== "+" && last !== "" && newlast === "+"){
          console.log("WIN!");
          playSound(73);
        } else if (last !== "-" && last !== "" && newlast === "-"){
          console.log("LOSE!");
          playSound(74);
        }

        if ((ot_newgamehist.innerHTML).charAt(0) === "[")
          needplayendrefresh = true;

      }
    }

    if (needplayrefresh) {
      if (needplayendrefresh) {
        btn = document.getElementById("pageform:btn_playendrefresh");
      } else {
        btn = document.getElementById("pageform:btn_playrefresh");
      }

      if (btn !== null) {
        btn.click();
      }
    }
  }

  if (!needplayrefresh) {
    var ot_newboardhits = document.getElementById("pageform:ot_boardhits2");
    if (ot_newboardhits !== null) {
      var cv_board = document.getElementById("cv_board");
      var ot_prevboardhits = document.getElementById("pageform:ot_boardhits");
      if (cv_board !== null) {
        if (ot_prevboardhits.innerHTML !== ot_newboardhits.innerHTML) {
          ot_prevboardhits.innerHTML = ot_newboardhits.innerHTML;

          var ctx = cv_board.getContext("2d");
          if (ot_newboardhits.innerHTML === "") {
            ctx.clearRect(0, 0, cv_board.width, cv_board.height);
          } else {
            var hitsstr = ot_newboardhits.innerHTML;
            cv_board.width = 186;
            cv_board.height = 186;
            var pos = 0;
            while (pos < hitsstr.length) {
              if (hitsstr.charAt(pos) === "H" || hitsstr.charAt(pos) === "V") {
                var horizontal = (hitsstr.charAt(pos) === "H");
                var line = hitsstr.charAt(pos + 1);
                var startpos = hitsstr.charAt(pos + 2);
                var length = hitsstr.charAt(pos + 3);
                drawHitRect(ctx, horizontal, line, startpos, length);
              }
              pos += 4;
            }
          }
        }
      }
    }
  }

}

function drawHitRect(ctx, horizontal, line, startpos, length) {
  const cornerx = 2;
  const cornery = 2;

  var startx = cornerx + 31 * startpos;
  var starty = cornerx + 31 * line;
  var widthx = 31 * length - 4;
  var widthy = 23;

  ctx.beginPath();
  ctx.lineWidth = 1.5;
  if (horizontal === true) {
    starty = starty + 2;
    ctx.setLineDash([4, 2]);
    ctx.strokeStyle = "rgb(219, 50, 50)";
    ctx.rect(startx, starty, widthx, widthy);
  } else {
    starty = starty + 2;
    ctx.setLineDash([0, 2, 4, 0]);
    ctx.strokeStyle = "rgb(108, 168, 83)";
    ctx.rect(starty, startx, widthy, widthx);
  }
  ctx.stroke();
}
