/*
 * Copyright 2024 Stanislav Aleshin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package platform

import dev.icerock.moko.parcelize.Parcel
import dev.icerock.moko.parcelize.Parceler
import kotlinx.datetime.Instant

/**
 * @author Stanislav Aleshin on 03.06.2024.
 */
object InstantParceler : Parceler<Instant> {

    override fun create(parcel: Parcel): Instant {
        return Instant.parse(checkNotNull(parcel.readString()))
    }

    override fun Instant.write(parcel: Parcel, flags: Int) {
        parcel.writeString(this.toString())
    }
}

object NullInstantParceler : Parceler<Instant?> {

    override fun create(parcel: Parcel): Instant? {
        return parcel.readString()?.let { Instant.parse(it) }
    }

    override fun Instant?.write(parcel: Parcel, flags: Int) {
        if (this != null) parcel.writeString(this.toString())
    }
}