package cache

import com.google.devtools.clouderrorreporting.v1beta1.ServiceContext

class SeenKey(val services : List<ServiceContext>, val groupId : String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SeenKey) return false

        if (services != other.services) return false
        if (groupId != other.groupId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = services.hashCode()
        result = 31 * result + groupId.hashCode()
        return result
    }
}