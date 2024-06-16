package com.fibelatti.core.functional

public interface Mapper<InType, OutType> {

    public fun map(param: InType): OutType

    public fun mapList(param: List<InType>): List<OutType> = param.map(::map)
}

public interface TwoWayMapper<InType, OutType> : Mapper<InType, OutType> {

    public fun mapReverse(param: OutType): InType

    public fun mapListReverse(param: List<OutType>): List<InType> = param.map(::mapReverse)
}
