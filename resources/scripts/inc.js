/* ===============================================================================
 *  Copyright (c) 2022 lorenzop
 *  <https://poixson.com>
 *  Released under the GPL 3.0
 * 
 *  Description: Common functions for ScriptKit scripts
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================
 */
// inc.js



var blocksBuffer = [ ];
var blockAliases = { };



function isNullOrEmpty(value) {
	if (!value)             return true;
	if (value == undefined) return true;
	if (value == "")        return true;
	if (value.length == 0)  return true;
	return false;
}



function ReplaceAt(str, index, replace) {
	return str.substr(0, index) + replace + str.substr(index+replace.length);
}



function toHex(value) {
	let str = value.toString(16).toUpperCase();
	if (str.length == 0)
		return "x00";
	if (str.length == 1)
		return "x0"+str;
	return "x"+str;
}



// -------------------------------------------------------------------------------
// math



var angle45    = Math.PI * 0.25;
var angle45Sin = Math.sin(angle45);
var angle45Cos = Math.cos(angle45);

function rotX(x, z) {
	return (angle45Sin * z) - (angle45Cos * x);
}
function rotZ(x, z) {
	return (angle45Sin * x) + (angle45Cos * z);
}



// -------------------------------------------------------------------------------
// block alias



function setBlockAlias(key, type) {
	blockAliases[key] = type;
}

function getBlockType(type) {
	if (typeof type === "string") {
		if (blockAliases.hasOwnProperty(type))
			return getBlockType( blockAliases[type] );
	}
	return BukkitUtils.ParseBlockType(type);
}



// -------------------------------------------------------------------------------
// set blocks



function commitBlocks(chunk) {
	chunk.setBlocksJS(blocksBuffer);
	blocksBuffer = [ ];
}

function setBlock(x, y, z, block) {
	blocksBuffer.push({
		x:x, y:y, z:z,
		type: block
	});
}

function unsetBlock(x, y, z) {
	let found = false;
	blocksBuffer.forEach(
		function(value, index, array) {
			if (value == null)
				return;
			if (value.x == x
			&&  value.y == y
			&&  value.z == z) {
				found = true;
				blocksBuffer[index] = null;
			}
		}
	);
	return found;
}



function drawFrame(x, y, z, w, h, d, block) {
	// bottom lines
	if (w < 0) { x += w+1; w = 0 - w; }
	if (h < 0) { y += h+1; h = 0 - h; }
	if (d < 0) { z += d+1; d = 0 - d; }
	// bottom lines
	for (let i=0; i<w; i++) setBlock(x+i,   y, z,     block);
	for (let i=0; i<d; i++) setBlock(x,     y, z+i,   block);
	for (let i=0; i<d; i++) setBlock(x+w-1, y, z+i,   block);
	for (let i=0; i<w; i++) setBlock(x+i,   y, z+d-1, block);
	// vertical lines
	for (let i=0; i<h; i++) setBlock(x,     y+i, z,     block);
	for (let i=0; i<h; i++) setBlock(x+w-1, y+i, z,     block);
	for (let i=0; i<h; i++) setBlock(x,     y+i, z+d-1, block);
	for (let i=0; i<h; i++) setBlock(x+w-1, y+i, z+d-1, block);
	// top lines
	for (let i=0; i<w; i++) setBlock(x+i,   y+h-1, z,     block);
	for (let i=0; i<d; i++) setBlock(x,     y+h-1, z+i,   block);
	for (let i=0; i<w; i++) setBlock(x+i,   y+h-1, z+d-1, block);
	for (let i=0; i<d; i++) setBlock(x+w-1, y+h-1, z+i,   block);
}



function setBlockMatrix(blocks, matrix, axis, x, y, z) {
	if (isNullOrEmpty(blocks)) return;
	if (isNullOrEmpty(matrix)) return;
	if (isNullOrEmpty(axis))   return;
	let stepX = 0;
	let stepY = 0;
	let stepZ = 0;
	let len = axis.length;
	let ax = axis.charAt(len - 1);
	axis = axis.substr(0, len -1);
	switch (ax) {
		case "Z": case "n": stepZ = -1; break;
		case "z": case "s": stepZ =  1; break;
		case "x": case "e": stepX =  1; break;
		case "X": case "w": stepX = -1; break;
		case "Y": case "u": stepY =  1; break;
		case "y": case "d": stepY = -1; break;
		default:
			out.println("error: Unknown SetBlock1 axis: " + ax);
			return;
	}
	len = matrix.length;
	let xx, yy, zz;
	let matrix_last = (typeof matrix == "string");
	for (let i=0; i<len; i++) {
		xx = x + (i * stepX);
		yy = y + (i * stepY);
		zz = z + (i * stepZ);
		// another dimension
		if (!matrix_last) {
			setBlockMatrix(
				blocks,
				matrix[i],
				xx, yy, zz,
				axis
			);
			continue;
		}
		// last dimension
		let blk = matrix.charAt(i);
		if (isNullOrEmpty(blk)) continue;
		if (blk == " ") continue;
		if (blk in blocks)
			blk = blocks[blk];
		setBlock(xx, yy, zz, blk);
	}
}



function FillXYZ(x, y, z, w, h, d, block) {
	if (w < 0) { x += w+1; w = 0 - w; }
	if (h < 0) { y += h+1; h = 0 - h; }
	if (d < 0) { z += d+1; d = 0 - d; }
	for (let yy=h; yy>=0; yy--) {
		for (let zz=0; zz<d; zz++) {
			for (let xx=0; xx<w; xx++) {
				setBlock(xx+x, yy+y, zz+z, block);
			}
		}
	}
}



// -------------------------------------------------------------------------------
// axis



function AxisToRotations(axis) {
	if (isNullOrEmpty(axis))
		return null;
	let ax = axis.charAt(0);
	let rotations = {
		N: AxisToFacing(            axis.charAt(0)       ),
		S: AxisToFacing( RotateAxis(axis.charAt(0), 180) ),
		E: AxisToFacing(            axis.charAt(1)       ),
		W: AxisToFacing( RotateAxis(axis.charAt(1), 180) ),
		NS: (
			ax=="n" || ax=="s" ||
			ax=="Z" || ax=="z"
			? "ns" : "ew"
		),
		EW: (
			ax=="e" || ax=="w" ||
			ax=="X" || ax=="x"
			? "ns" : "ew"
		)
	};
	return rotations;
}



function FacingToAxis(facing) {
	if (facing == null)
		return null;
	let result = "";
	let len = facing.length;
	for (let i=0; i<len; i++) {
		let chr = facing.charAt(i);
		switch (chr) {
			case "n": result += "Z"; break;
			case "s": result += "z"; break;
			case "e": result += "x"; break;
			case "w": result += "X"; break;
			default:  result += chr; break;
		}
	}
	return result;
}



function AxisToFacing(ax) {
	switch (ax) {
		case "Z": return "n";
		case "z": return "s";
		case "x": return "e";
		case "X": return "w";
		case "Y": return "u";
		case "y": return "d";
		default: break;
	}
	return ax;
}



function LocAddAxis(loc, axis, distance) {
	let result = {
		"x": loc.x,
		"y": loc.y,
		"z": loc.z
	};
	let len = axis.length;
	for (let i=0; i<len; i++) {
		let ax = axis.charAt(i);
		switch (ax) {
			case "Z": case "n": result.z -= distance; break;
			case "z": case "s": result.z += distance; break;
			case "x": case "e": result.x += distance; break;
			case "X": case "w": result.x -= distance; break;
			case "y": case "u": result.y += distance; break;
			case "Y": case "d": result.y -= distance; break;
			default:
				out.println("error: Unknown LocAddAxis axis: " + ax);
				return null;
		}
	}
	return result;
}



function RotateAxis(axis, angle) {
	if (axis == null)
		return null;
	let len = axis.length;
	angle = angle % 360;
	if (angle < 0)
		angle = 360 + angle;
	let axs = axis;
	let result = "";
	while (angle >= 90) {
		result = "";
		for (let i=0; i<len; i++) {
			let chr = axs.charAt(i);
			if (chr == "y" || chr == "Y") {
				result += chr;
				continue;
			}
			switch (chr) {
				case "Z": case "n": result += "x"; break;
				case "z": case "s": result += "X"; break;
				case "x": case "e": result += "z"; break;
				case "X": case "w": result += "Z"; break;
				default:
					out.println("error: Unknown LocAddAxis axis: " + chr);
					return null;
			}
		}
		angle -= 90;
		axs = result;
	}
	return result;
}
