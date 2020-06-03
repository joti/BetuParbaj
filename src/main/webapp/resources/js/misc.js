function letterstobg() {

  var cv_menu = document.getElementById("cv_menu");
  if (cv_menu !== null) {
    var ot_randomletters = document.getElementById("pageform:ot_randomletters");
    var lettersstr = ot_randomletters.innerHTML;
    var letters = lettersstr.split(".");
    var captions = ["", "Ú", "J", "", "J", "Á", "T", "SZ", "M", "A",
                    "", "I", "N", "D", "Í", "T", "Á", "S", "A", "", 
                    "", "", "J", "Á", "T", "SZ", "M", "A", "", "",
                    "F", "O", "L", "Y", "T", "A", "T", "Á", "S", "A",
                    "", "", "I", "N", "D", "U", "L", "Ó", "", "",
                    "", "J", "Á", "T", "S", "Z", "M", "Á", "K", "",
                    "", "", "K", "O", "R", "Á", "B", "B", "I", "",
                    "E", "R", "E", "D", "M", "É", "N", "Y", "E", "K",
                    "", "A", "", "J", "Á", "T", "É", "K", "", "",
                    "", "S", "Z", "A", "B", "Á", "LY", "A", "I", "",
                    "", "", "S", "Z", "Ó", "T", "Á", "R", "", "",
                    "", "B", "Ő", "V", "Í", "T", "É", "S", "E", "",
                    "", "", "K", "I", "L", "É", "P", "É", "S", "",
                    "A", "", "J", "Á", "T", "É", "K", "B", "Ó", "L"];
    
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
        if (captions[index] !== ""){
          letter = captions[index];
          ctx.fillStyle = "#707561";
          if (wstart < 0)
            wstart = col;
        } else {
          letter = letters[index];
          ctx.fillStyle = "rgba(128,133,113,0.25)";
          if (wstart >= 0){
            wlength = col - wstart;
          }
        }  
        if (col === 9 && wstart >= 0 && wlength === 0){
          wlength = 10 - wstart;
        }

        /* 2. paraméter: horizontális, 3. paraméter: vertikális pozíció */
        ctx.fillText(letter, 31 * col + 17, 31 * row + 23, 40);
        
        if (wlength > 0){
          drawWordRect(ctx, row, wstart, wlength, (row % 4 === 0 || row %4 === 1 ? true : false) );
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
  
  if (altcolor){
    ctx.strokeStyle = "rgb(108, 168, 83)";
  } else {
    ctx.strokeStyle = "rgb(219, 50, 50)";
  }  
    
  ctx.rect(startx, starty, widthx, widthy);
  
  ctx.stroke();
}

