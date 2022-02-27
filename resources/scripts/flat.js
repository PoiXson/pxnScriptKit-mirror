/* ===============================================================================
 *  Copyright (c) 2022 lorenzop
 *  <https://poixson.com>
 *  Released under the GPL 3.0
 * 
 *  Description: Script to generate a flat world
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
// flat.js



// example:
// generator: ScriptKit:flat.js 1=bedrock,1=stone
// args = "1=bedrock,5=stone,1=moss_block";



importClass(Packages.com.poixson.commonbukkit.utils.BukkitUtils);



function generate(chunk) {
//	out.println("CHUNK x: " + chunk.absX + " z: " + chunk.absZ);
	let buffer = [ ];
//TODO
var args = "1=bedrock,2=stone,3=dirt";
	var argsArray = args.split(",");
	var yy = -64;
	for (var index=0; index<argsArray.length; index++) {
		var parts = argsArray[index].split("=");
		var height = Number( parts[0] );
		let block = BukkitUtils.ParseBlockData(parts[1]);
		for (var y=0; y<height; y++) {
			for (var z=0; z<16; z++) {
				for (var x=0; x<16; x++) {
					buffer.push({ type: block, x:x, y:y+yy, z:z });
				} // end x
			} // end z
		} // end y
		yy += height;
	} // end index
	chunk.setBlocksJS(buffer);
}
