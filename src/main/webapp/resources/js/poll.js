function setMessages(xhr, status, args) {
  var scrollPos;
  var scrollHgt;
  var prevVal;
  var newVal;
  var btn;

  var ita_prevnames = document.getElementById("pageform:ita_names");
  if (ita_prevnames !== null) {
    var ita_newnames = document.getElementById("pageform:ita_names2");

    if (ita_prevnames.value !== ita_newnames.value) {
      scrollPos = ita_prevnames.scrollTop;
      scrollHgt = ita_prevnames.scrollHeight;

      ita_prevnames.value = ita_newnames.value;

      ita_prevnames.scrollTop = scrollPos + ita_prevnames.scrollHeight - scrollHgt;
      if ($(".namesscroll").getNiceScroll() !== null) {
        $(".namesscroll").getNiceScroll().resize();
      }
    }
  }

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
//        btn.dispatchEvent(new Event("click"));
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
//          btn.dispatchEvent(new Event("click"));
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
//        btn.dispatchEvent(new Event("click"));
        btn.click();
      }
    }
  }

  var ot_newturnsec = document.getElementById("pageform:ot_turnsec2");
  if (ot_newturnsec !== null) {
    var ot_playerstate = document.getElementById("pageform:ot_playerstate");
    var ot_prevturnsec = document.getElementById("pageform:ot_turnsec");

    if (ot_prevturnsec !== null) {
      if (ot_prevturnsec.innerHTML !== ot_newturnsec.innerHTML) {
        ot_prevturnsec.innerHTML = ot_newturnsec.innerHTML;
        if ((ot_playerstate.innerHTML < 1) || (ot_prevturnsec.innerHTML > 999)) {
          ot_prevturnsec.style.display = "none";
        } else {
          ot_prevturnsec.style.display = "block";
          if (ot_playerstate.innerHTML > 2) {
            ot_prevturnsec.style.color = "#dee4b9";
          } else if (ot_prevturnsec.innerHTML < 10) {
            ot_prevturnsec.style.color = "red";
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
        ot_prevgamehist.innerHTML = ot_newgamehist.innerHTML;
        needplayrefresh = true;

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
//        btn.dispatchEvent(new Event("click"));
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
  const cornerx = 4;
  const cornery = 4;

  var startx = cornerx + 31 * startpos;
  var starty = cornerx + 31 * line;
  var widthx = 31 * length - 8;
  var widthy = 23;

  ctx.beginPath();
  ctx.setLineDash([2, 2]);
  if (horizontal === true) {
    ctx.strokeStyle = "rgb(255, 0, 0)";
    ctx.rect(startx, starty, widthx, widthy);
  } else {
    ctx.strokeStyle = "rgb(102, 150, 83)";
    ctx.rect(starty, startx, widthy, widthx);
  }
  ctx.stroke();
}
