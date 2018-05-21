package d3debug.views.assetdata

import d3cp.AssetCp
import d3debug.domain.Asset
import d3debug.domain.AssetType
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.image.ImageView
import javafx.scene.shape.Mesh
import javafx.scene.shape.TriangleMesh
import javafx.scene.shape.VertexFormat
import java.nio.ByteOrder

class AssetDataMesh(val reader: AssetCp.AssetMeshData.Reader) : AssetData() {
    init {
        previewNode.set(ImageView(Global.fallbackImage))
    }

    override fun loadSync() {
    }

    override val previewSize = SimpleDoubleProperty(this, "previewSize", 100.0)

    override val assetType = AssetType.Mesh


    fun readMeshes(assetFactory: (String) -> Asset?): Sequence<Pair<AssetDataMaterialDesc, Mesh>> {


        if (!reader.hasAttributeArrayInterleavedList()) {
            return emptySequence()
        }

        val appearancesList = reader.appearanceList.asSequence().map { assetFactory(it.toString()) }

        val meshes = appearancesList.zip(reader.attributeArrayInterleavedList.asSequence()).map { (app, array) ->
            //            app ?: return@map null

            if (app == null || app.assetType != AssetType.Appearance ) {
                return@map null
            }

            val appData = (createAssetData(app) as? AssetDataMaterialDesc)
            appData ?: return@map null

            val position = array.attributes.find {
                it.hasName() && it.name.toString() == "a_position"
            } ?: return@map null

            val texcoord = array.attributes.find {
                it.hasName() && it.name.toString() == "a_texcoord"
            } ?: return@map null


            val mesh = TriangleMesh(VertexFormat.POINT_TEXCOORD)

            val numVertex = array.numVertex
            val points = mesh.points
            points.resize(numVertex * 3)

            val texCoords = mesh.texCoords
            texCoords.resize(numVertex * 2)

            val attributeBuffer = array.attributeArray.asByteBuffer()

            for (i in 0 until numVertex) {

                attributeBuffer.position(i * array.attributeStride + position.offset)
                val positionArray = attributeBuffer.slice().order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()


                points.set(i * 3 + 0, positionArray[0])
                points.set(i * 3 + 1, positionArray[1])
                points.set(i * 3 + 2, positionArray[2])


                attributeBuffer.position(i * array.attributeStride + texcoord.offset)
                val texcoordBuffer = attributeBuffer.slice().order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()

                texCoords.set(i * 2 + 0, texcoordBuffer[0])
                texCoords.set(i * 2 + 1, texcoordBuffer[1])
            }

            val indexArray = array.indexArray.asByteBuffer().order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()

            val numIndex = array.numIndex
            val faces = mesh.faces
            faces.resize(numIndex * 2)
            for (i in 0 until numIndex step 3) {
                // change winding to CCW
                faces.set(i * 2 + 0, indexArray[i + 2].toInt())
                faces.set(i * 2 + 1, indexArray[i + 2].toInt())
                faces.set(i * 2 + 2, indexArray[i + 1].toInt())
                faces.set(i * 2 + 3, indexArray[i + 1].toInt())
                faces.set(i * 2 + 4, indexArray[i].toInt())
                faces.set(i * 2 + 5, indexArray[i].toInt())
            }

            Pair(appData, mesh)
        }.filterNotNull()

        return meshes
    }


}