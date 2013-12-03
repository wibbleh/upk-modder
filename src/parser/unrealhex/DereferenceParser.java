/*
 * Copyright (C) 2013 Rachel Norman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package parser.unrealhex;

import model.modfile.ModLine;

/**
 *
 * @author Amineri
 */


public class DereferenceParser
{
    public String parseString(ModLine line)
    {
        String out = "";
        String comment = "";
        for(int i=0;i<line.getIndentation();i++)
        {
            out += "\t";
        }
        if(line.asString().contains("//"))
        {
            comment = line.asString().split("//")[1];
        }
        String[] tokens = line.asString().split("\\s");
        for(String token : tokens)
        {
            if(token.equals("{{") || token.equals("}}") || token.equals("<<") || token.equals(">>"))
            {
            }
            else
            {
                out += token + " ";
            }
        }
        out += "// " + comment;
        return out;
    }

}
