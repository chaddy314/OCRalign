const UP   = 1;
const LEFT = 2;
const UL   = 4;

const SYMBOL = "֍";

function nw(s1, s2, op) {
    op = op || {};
    const G = op.G || 2;
    const P = op.P || 1;
    const M = op.M || -1;
    var mat   = {};
    var direc = {};

    // initialization
    for(var i=0; i<s1.length+1; i++) {
        mat[i] = {0:0};
        direc[i] = {0:[]};
        for(var j=1; j<s2.length+1; j++) {
            mat[i][j] = (i == 0)
                ? 0
                : (s1.charAt(i-1) == s2.charAt(j-1)) ? P : M
            direc[i][j] = [];
        }
    }

    // calculate each value
    for(var i=0; i<s1.length+1; i++) {
        for(var j=0; j<s2.length+1; j++) {
            var newval = (i == 0 || j == 0)
                ? -G * (i + j)
                : Math.max(mat[i-1][j] - G, mat[i-1][j-1] + mat[i][j], mat[i][j-1] -G);

            if (i > 0 && j > 0) {
                if( newval == mat[i-1][j] - G) direc[i][j].push(UP);
                if( newval == mat[i][j-1] - G) direc[i][j].push(LEFT);
                if( newval == mat[i-1][j-1] + mat[i][j]) direc[i][j].push(UL);
            }
            else {
                direc[i][j].push((j == 0) ? UP : LEFT);
            }
            mat[i][j] = newval;
        }
    }

    // get result
    var chars = [[],[]];
    var I = s1.length;
    var J = s2.length;
    const max = Math.max(I, J);
    while(I > 0 || J > 0) {
        switch (direc[I][J][0]) {
            case UP:
                I--;
                chars[0].push(s1.charAt(I));
                chars[1].push(SYMBOL);
                break;
            case LEFT:
                J--;
                chars[0].push('-');
                chars[1].push(s2.charAt(J));
                break;
            case UL:
                I--;
                J--;
                chars[0].push(s1.charAt(I));
                chars[1].push(s2.charAt(J));
                break;
            default: break;
        }
    }

    return chars.map(function(v) {
        return v.reverse().join('');
    });
}


function main() {

    var w1 = document.getElementById("w1").value;
    var w2 = document.getElementById("w2").value;

    var r = nw(w1,w2);

    //document.getElementById("r1").innerHTML() = r;
    console.log(r[0]);
    console.log(r[1]);
    //document.getElementById("r2").innerHTML = r[0];
    //document.getElementById("r3").innerHTML = r[1];
}