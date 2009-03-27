//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/
package org.deegree.rendering.r3d.opengl.rendering.dem;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.media.opengl.GL;

import org.deegree.rendering.r3d.multiresolution.MeshFragment;
import org.deegree.rendering.r3d.multiresolution.MeshFragmentData;
import org.deegree.rendering.r3d.multiresolution.MultiresolutionMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates a {@link MeshFragment} of a {@link MultiresolutionMesh} that can be rendered via JOGL.
 * <p>
 * The geometry data of a {@link RenderableMeshFragment} has one of the following states:
 * <ul>
 * <li>Data is not loaded</li>
 * <li>Data loaded to main memory (i.e. buffer objects are created and filled)</li>
 * <li>Data loaded to GPU (i.e. OpenGL VBOs created and loaded to the GPU)</li>
 * <li>Data loaded to main memory and GPU</li>
 * </ul>
 * </p>
 * <p>
 * A {@link RenderableMeshFragment} can be associated with a {@link MeshFragmentTexture}. In this case, it is rendered
 * with an applied texture.
 * </p>
 * 
 * @see MultiresolutionMesh
 * @see MeshFragment
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class RenderableMeshFragment implements Comparable<RenderableMeshFragment> {

    private static final Logger LOG = LoggerFactory.getLogger( RenderableMeshFragment.class );

    private final MeshFragment fragment;

    private MeshFragmentData data;

    private MeshFragmentTexture texture;

    // texture that is bound to glBufferObjectIds[3]
    private MeshFragmentTexture enabledTexture;

    // 0: vertex (coordinates) buffer
    // 1: normal buffer
    // 2: triangle buffer
    // 3: texture coordinates buffer
    private int[] glBufferObjectIds;

    public RenderableMeshFragment( MeshFragment fragment ) {
        this.fragment = fragment;
    }

    public float[][] getBBox() {
        return fragment.bbox;
    }

    public float getGeometricError() {
        return fragment.error;
    }

    public MeshFragmentData getData() {
        return data;
    }

    /**
     * Returns the applied texture.
     * 
     * @return the applied texture
     */
    public MeshFragmentTexture getTexture() {
        return texture;
    }

    public void setTexture( MeshFragmentTexture texture ) {
        if ( this.texture != null && this.texture != texture ) {
            this.texture.buffer.free();
        }
        this.texture = texture;
    }

    /**
     * Returns the resolution of the applied texture (meters per pixel).
     * 
     * @return the resolution of the applied texture, or -1 if no texture is applied
     */
    public float getTextureResolution() {
        if ( texture != null ) {
            return texture.getTextureResolution();
        }
        return -1.0f;
    }

    /**
     * Returns whether the geometry data is available in main memory.
     * 
     * @return true, if the geometry data is available in main memory, false otherwise
     */
    public boolean isLoaded() {
        return data != null;
    }

    /**
     * Loads the geometry data into main memory.
     * 
     * @throws IOException
     */
    public void load()
                            throws IOException {
        if ( data == null ) {
            data = fragment.loadData();
        }
    }

    /**
     * Removes the geometry data from main memory (and disables it).
     */
    public void unload() {
        if ( data != null ) {
            data.freeBuffers();
            data = null;
        }
        if ( enabledTexture != null ) {
            texture.buffer.free();
            texture = null;
        }
    }

    /**
     * Returns whether fragment is ready for rendering (prepared VBOs).
     * 
     * @return true, if the fragment is ready to be rendered
     */
    public boolean isEnabled() {
        return glBufferObjectIds != null && (enabledTexture == texture || texture == null);
    }

    /**
     * Enables the fragment in the given OpenGL context, so it can be rendered.
     * 
     * @param gl
     * @throws IOException
     */
    public void enable( GL gl )
                            throws IOException {

        if ( data == null ) {
            load();
        }
        if ( glBufferObjectIds == null ) {
            glBufferObjectIds = new int[4];
            gl.glGenBuffersARB( 4, glBufferObjectIds, 0 );

            FloatBuffer vertexBuffer = data.getVertices();
            ShortBuffer indexBuffer = (ShortBuffer) data.getTriangles();
            FloatBuffer normalsBuffer = data.getNormals();

            // bind vertex buffer object (vertices)
            gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[0] );
            gl.glBufferDataARB( GL.GL_ARRAY_BUFFER_ARB, vertexBuffer.capacity() * 4, vertexBuffer,
                                GL.GL_STATIC_DRAW_ARB );

            // bind vertex buffer object (normals)
            gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[1] );
            gl.glBufferDataARB( GL.GL_ARRAY_BUFFER_ARB, normalsBuffer.capacity() * 4, normalsBuffer,
                                GL.GL_STATIC_DRAW_ARB );

            // bind element buffer object (triangles)
            gl.glBindBufferARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, glBufferObjectIds[2] );
            gl.glBufferDataARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, indexBuffer.capacity() * 2, indexBuffer,
                                GL.GL_STATIC_DRAW_ARB );
        }
        if ( texture != null && texture != enabledTexture ) {

            if (enabledTexture != null) {
                enabledTexture.disable( gl );
            }
            
            // bind vertex buffer object (vertex coordinates)
            gl.glBindBufferARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, glBufferObjectIds[3] );
            gl.glBufferDataARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, texture.texCoordsBuffer.capacity() * 4,
                                texture.texCoordsBuffer, GL.GL_STATIC_DRAW_ARB );
            enabledTexture = texture;            
        }
    }

    /**
     * Disables the fragment in the given OpenGL context and frees the associated VBOs and texture.
     * 
     * @param gl
     */
    public void disable( GL gl ) {
        if ( glBufferObjectIds != null ) {
            int[] bufferObjectIds = this.glBufferObjectIds;
            this.glBufferObjectIds = null;
            gl.glDeleteBuffersARB( bufferObjectIds.length, bufferObjectIds, 0 );
        }
        if ( enabledTexture != null ) {
            enabledTexture.disable( gl );
            enabledTexture = null;
        }
    }

    /**
     * Renders this fragment to the given OpenGL context.
     * 
     * @param gl
     * @throws RuntimeException
     *             if the geometry data is currently not bound to VBOs
     */
    public void render( GL gl ) {

        if ( !isEnabled() ) {
            throw new RuntimeException( "Cannot render mesh fragment, not enabled." );
        }

        // render with or without texture
        if ( texture != null ) {
            // texture has been enabled and texture coordinate buffer has been prepared
            gl.glEnable( GL.GL_TEXTURE_2D );
            gl.glBindTexture( GL.GL_TEXTURE_2D, texture.getGLTextureId( gl ) );
            gl.glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY );

            gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[3] );
            gl.glTexCoordPointer( 2, GL.GL_FLOAT, 0, 0 );
        } else {
            gl.glDisable( GL.GL_TEXTURE_2D );
            gl.glDisableClientState( GL.GL_TEXTURE_COORD_ARRAY );
        }

        gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[0] );
        gl.glVertexPointer( 3, GL.GL_FLOAT, 0, 0 );

        gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[1] );
        gl.glNormalPointer( GL.GL_FLOAT, 0, 0 );

        gl.glBindBufferARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, glBufferObjectIds[2] );
        gl.glDrawElements( GL.GL_TRIANGLES, data.getNumTriangles() * 3, GL.GL_UNSIGNED_SHORT, 0 );
    }

    @Override
    public int compareTo( RenderableMeshFragment o ) {
        return this.fragment.compareTo( o.fragment );
    }

}
