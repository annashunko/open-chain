package io.openfuture.chain.repository

import io.openfuture.chain.config.RepositoryTests
import io.openfuture.chain.entity.SeedWord
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class SeedWordRepositoryTests : RepositoryTests() {

    @Autowired
    private lateinit var repository: SeedWordRepository


    @Test
    fun findOneByWordIndexTest() {
        val wordIndex = 2048
        val expectedSeedWord = SeedWord(2048, "zop")

        entityManager.persist(expectedSeedWord)

        val seedWordResult = repository.findOneByIndex(wordIndex)

        assertThat(seedWordResult).isEqualTo(expectedSeedWord)
    }

}