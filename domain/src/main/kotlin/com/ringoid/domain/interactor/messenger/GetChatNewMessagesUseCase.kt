package com.ringoid.domain.interactor.messenger

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.exception.MissingRequiredParamsException
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.interactor.base.processSingle
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.messenger.Chat
import com.ringoid.domain.repository.messenger.IMessengerRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Same as [GetChatUseCase], but retain only new messages in result data. Message is considered
 * as 'new' if it hasn't been stored locally yet.
 */
class GetChatNewMessagesUseCase @Inject constructor(private val repository: IMessengerRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Chat>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Chat> {
        val chatId = params.get<String>("chatId")
        val sourceFeed = params.get<String>("sourceFeed") ?: DomainUtil.SOURCE_FEED_MESSAGES

        return if (chatId.isNullOrBlank()) {
            Single.error(MissingRequiredParamsException())
        } else {
            params.processSingle(ImageResolution::class.java) {
                repository.getChatNew(chatId = chatId, resolution = it, sourceFeed = sourceFeed)
            }
        }
    }
}