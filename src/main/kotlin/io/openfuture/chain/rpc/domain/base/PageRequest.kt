package io.openfuture.chain.rpc.domain.base

import org.springframework.data.domain.AbstractPageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

open class PageRequest(
    @field:Min(value = 0) private var offset: Long = 0,
    @field:Min(value = 1) @field:Max(100) private var limit: Int = 100,
    @field:NotBlank private var sortField: String? = null,
    private var sortDirection: Direction? = null
) : AbstractPageRequest(offset.toInt() / limit + 1, limit) {

    override fun next(): Pageable = PageRequest(offset + limit, limit)

    override fun getOffset(): Long = offset

    fun getLimit(): Int = limit

    override fun getSort(): Sort = Sort(sortDirection ?: Direction.ASC, sortField ?: "id")

    override fun first(): Pageable = PageRequest(0, limit)

    override fun previous(): PageRequest {
        return if (offset == 0L) this else {
            var newOffset = this.offset - limit
            if (newOffset < 0) newOffset = 0
            PageRequest(newOffset, limit)
        }
    }

}