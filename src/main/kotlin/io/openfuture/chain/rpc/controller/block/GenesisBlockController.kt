package io.openfuture.chain.rpc.controller.block

import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.block.GenesisBlockResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rpc/blocks/genesis")
class GenesisBlockController(
    private val blockService: GenesisBlockService
) {

    @GetMapping("/{hash}")
    fun get(@PathVariable hash: String): GenesisBlockResponse = GenesisBlockResponse(blockService.getByHash(hash))

    @GetMapping("/{hash}/previous")
    fun getPreviousBlock(@PathVariable hash: String): GenesisBlockResponse? =
        GenesisBlockResponse(blockService.getPreviousBlock(hash))

    @GetMapping("/{hash}/next")
    fun getNextBlock(@PathVariable hash: String): GenesisBlockResponse? =
        GenesisBlockResponse(blockService.getNextBlock(hash))

    @GetMapping
    fun getAll(request: PageRequest): PageResponse<GenesisBlockResponse> =
        PageResponse(blockService.getAll(request).map { GenesisBlockResponse(it) })

}