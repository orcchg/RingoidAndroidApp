package com.orcchg.githubuser.data.executor

import com.orcchg.githubuser.domain.executor.UseCaseThreadExecutor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class UseCaseThreadExecutorImpl : UseCaseThreadExecutor {

    private val executor = ThreadPoolExecutor(4, 8, 10, TimeUnit.SECONDS,
                                              LinkedBlockingQueue(), UseCaseThreadFactory())

    override fun execute(command: Runnable) {
        executor.execute(command)
    }
}

class UseCaseThreadFactory : ThreadFactory {

    companion object {
        private var INDEX = 0
    }

    override fun newThread(command: Runnable): Thread = Thread(command, "useCase_thread_${INDEX++}")
}
