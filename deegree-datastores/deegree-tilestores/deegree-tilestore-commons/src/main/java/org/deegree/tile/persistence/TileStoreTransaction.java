//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence;

import org.deegree.geometry.Envelope;
import org.deegree.tile.Tile;
import org.deegree.tile.TileIOException;

/**
 * Provides transactional access to a {@link TileStore}.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface TileStoreTransaction {

    /**
     * Updates or adds a specific tile.
     * 
     * @param tileMatrix
     *            the tile matrix id
     * @param tile
     *            the tile
     * @param x
     *            column index
     * @param y
     *            row index
     * @throws TileIOException 
     */
    void put( String tileMatrix, Tile tile, int x, int y ) throws TileIOException;

    /**
     * Deletes a specific tile.
     * 
     * @param tileMatrix
     *            the tile matrix id
     * @param x
     *            column index
     * @param y
     *            row index
     */
    void delete( String tileMatrix, int x, int y );

    /**
     * Deletes all tiles intersecting with the given envelope. If tileMatrix is null, tiles will be deleted from all tile
     * matrices.
     * 
     * @param tileMatrix
     *            the tile matrix id
     * @param envelope
     *            the envelope
     */
    void delete( String tileMatrix, Envelope envelope );
}
