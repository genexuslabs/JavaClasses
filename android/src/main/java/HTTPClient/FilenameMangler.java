
/*
 * @(#)FilenameMangler.java				0.3-3 06/05/2001
 *
 *  This file is part of the HTTPClient package
 *  Copyright (C) 1996-2001 Ronald Tschal�r
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  ronald@innovation.ch
 *
 *  The HTTPClient's home page is located at:
 *
 *  http://www.innovation.ch/java/HTTPClient/ 
 *
 */

package HTTPClient;


/**
 * {@link Codecs#mpFormDataDecode(byte[], java.lang.String, java.lang.String,
 * HTTPClient.FilenameMangler) Codecs.mpFormDataDecode} and {@link
 * Codecs#mpFormDataEncode(HTTPClient.NVPair[], HTTPClient.NVPair[],
 * HTTPClient.NVPair[], HTTPClient.FilenameMangler) Codecs.mpFormDataEncode}
 * may be handed an instance of a class which implements this interface in
 * order to control names of the decoded files or the names sent in the encoded
 * data.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschal�r
 * @since	V0.3-1
 */
public interface FilenameMangler
{
    /**
     * This is invoked by {@link Codecs#mpFormDataDecode(byte[],
     * java.lang.String, java.lang.String, HTTPClient.FilenameMangler)
     * Codecs.mpFormDataDecode} for each file found in the data, just before
     * the file is created and written. If null is returned then the file is
     * not created or written. This allows you to control which files are
     * written and the names of the resulting files.
     *
     * <P>For {@link Codecs#mpFormDataEncode(HTTPClient.NVPair[],
     * HTTPClient.NVPair[], HTTPClient.NVPair[], HTTPClient.FilenameMangler)
     * Codecs.mpFormDataEncode} this is also invoked on each filename, allowing
     * you to control the actual name used in the <var>filename</var> attribute
     * of the Content-Disposition header. This does not change the name of the
     * file actually read. If null is returned then the file is ignored.
     *
     * @param filename  the original filename in the Content-Disposition header
     * @param fieldname the name of the this field, i.e. the value of the
     *                  <var>name</var> attribute in Content-Disposition
     *                  header
     * @return the new file name, or null if the file is to be ignored.
     */
    public String mangleFilename(String filename, String fieldname);
}
