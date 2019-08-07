function setMessages(xhr, status, args) {
  console.log("setMessages");
  var scrollPos;
  var scrollHgt;
  var prevVal;
  var newVal;
  var btn;

  var ita_prevnames = document.getElementById("pageform:ita_names");
  if (ita_prevnames !== null) {
    console.log("scrolltop: " + ita_prevnames.scrollTop);
    var ita_newnames = document.getElementById("pageform:ita_names2");

    if (ita_prevnames.value !== ita_newnames.value) {
      console.log("names change");
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
      console.log("messages change");
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
      console.log("games change6");
      ita_prevgames.value = ita_newgames.value;

      btn = document.getElementById("pageform:btn_lobbyrefresh");
      if (btn !== null) {
        btn.dispatchEvent(new Event("click"));
      }  

    }
  }

  var ot_newgamestate = document.getElementById("pageform:ot_gamestate2");
  console.log(ot_newgamestate === null);
  console.log("a-a");
  if (ot_newgamestate !== null) {

    var ot_prevgamestate = document.getElementById("pageform:ot_gamestate");
    console.log(ot_prevgamestate.innerHTML);
    console.log(ot_newgamestate.innerHTML);
    if (ot_prevgamestate.innerHTML !== ot_newgamestate.innerHTML) {
      console.log("gamestate change");
      prevVal = ot_prevgamestate.innerHTML;
      newVal = ot_newgamestate.innerHTML;
      ot_prevgamestate.innerHTML = newVal;

      if ((newVal === "0" && prevVal !== newVal && prevVal !== "") // vissza a lobbiba
      || (newVal === "3" && prevVal !== newVal)) // indul a játék
      {
        console.log("mainrefresh előtt");
        btn = document.getElementById("pageform:btn_mainrefresh");
        if (btn !== null) {
          console.log("mainrefresh");
          btn.dispatchEvent(new Event("click"));
        }
      }  
    }    
  }
  console.log("b-b");

  var ot_newgamesetup = document.getElementById("pageform:ot_gamesetup2");
  console.log("gamesetup");
  console.log(ot_newgamesetup === null);
  if (ot_newgamesetup !== null) {
    console.log(ot_newgamesetup.innerHTML);
    var needsetuprefresh = false;
    var ot_prevgamesetup = document.getElementById("pageform:ot_gamesetup");

    if (ot_prevgamesetup !== null) {
      console.log(ot_prevgamesetup.innerHTML);
      if (ot_prevgamesetup.innerHTML !== ot_newgamesetup.innerHTML) {
        ot_prevgamesetup.innerHTML = ot_newgamesetup.innerHTML;
        needsetuprefresh = true;
        console.log(ot_prevgamesetup.innerHTML);
        console.log("c-c");
      }  
    }
    
    if (needsetuprefresh){
      btn = document.getElementById("pageform:btn_setuprefresh");
      if (btn !== null) {
        console.log("SETUPREFRESH");
        btn.dispatchEvent(new Event("click"));
      }
    }
  }

  console.log("ff-f");

  var ot_newturnsec = document.getElementById("pageform:ot_turnsec2");
  console.log("turnsec");
  console.log(ot_newturnsec === null);
  if (ot_newturnsec !== null) {
    console.log(ot_newturnsec.innerHTML);
    var ot_playerstate = document.getElementById("pageform:ot_playerstate");
    var ot_prevturnsec = document.getElementById("pageform:ot_turnsec");

    if (ot_prevturnsec !== null) {
      if (ot_prevturnsec.innerHTML !== ot_newturnsec.innerHTML) {
        ot_prevturnsec.innerHTML = ot_newturnsec.innerHTML;
        console.log(ot_playerstate.innerHTML);
        if (ot_playerstate.innerHTML < 1){
          console.log("display:none");
          ot_prevturnsec.style.display = "none";
        } else {
          ot_prevturnsec.style.display = "block";
          if (ot_playerstate.innerHTML > 2){
            console.log("#dee4b9");
            ot_prevturnsec.style.color = "#dee4b9";
          } else if (ot_prevturnsec.innerHTML < 10){
            console.log("red");
            ot_prevturnsec.style.color = "red";
          } else {
            console.log("#555c23");
            ot_prevturnsec.style.color = "#555c23";
          }
        }
        console.log("f-fff");
      }  
    }
  }  

  console.log("d-d");

  var ot_newgamehist = document.getElementById("pageform:ot_gamehist2");
  console.log("gamehist");
  console.log(ot_newgamehist === null);
  if (ot_newgamehist !== null) {
    console.log(ot_newgamehist.innerHTML);
    var needplayrefresh = false;
    var ot_prevgamehist = document.getElementById("pageform:ot_gamehist");

    if (ot_prevgamehist !== null) {
      console.log(ot_prevgamehist.innerHTML);
      if (ot_prevgamehist.innerHTML !== ot_newgamehist.innerHTML) {
        ot_prevgamehist.innerHTML = ot_newgamehist.innerHTML;
        needplayrefresh = true;
        console.log(ot_prevgamehist.innerHTML);
        console.log("e-e");
      }  
    }
    
    if (needplayrefresh){
      btn = document.getElementById("pageform:btn_playrefresh");
      if (btn !== null) {
        console.log("PLAYREFRESH");
        btn.dispatchEvent(new Event("click"));
      }
    }
  }

}


