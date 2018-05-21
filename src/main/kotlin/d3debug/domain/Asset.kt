/*
 * Copyright (c) 2018 Simon A. Berger
 * Licensed under the MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package d3debug.domain

import d3cp.AssetCp
import d3debug.loaders.AssetReaderFactory
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.image.Image
import tornadofx.*

enum class AssetType {
    Image,
    Appearance,
    Mesh,
    Unknown,
}


class Asset(val factory: AssetReaderFactory) {
    val uuidProperty = SimpleStringProperty(this, "uuid", factory.uuid)
    var uuid by uuidProperty

    val nameProperty = SimpleStringProperty(this, "name", factory.name)
    var name by nameProperty

    val imageProperty = SimpleObjectProperty<Image>(this, "image", null)
    var image2 by imageProperty

    var assetType: AssetType// = AssetType.Unknown

    val reader by lazy<AssetCp.Asset.Reader> {
        factory.reader
    }

    init {
        assetType = when (factory.reader.which()) {
            AssetCp.Asset.Which.PIXEL_DATA -> AssetType.Image
            AssetCp.Asset.Which.MESH_DATA -> AssetType.Mesh
            AssetCp.Asset.Which.MATERIAL_DESC -> AssetType.Appearance
            else -> AssetType.Unknown
        }
    }


}


