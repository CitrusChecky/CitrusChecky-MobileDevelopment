package com.rivaphys.citruschecky.utils

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.java.ChatFutures
import com.google.ai.client.generativeai.java.GenerativeModelFutures
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.rivaphys.citruschecky.data.Response
import java.util.concurrent.Executor

class CheckyAI {
    companion object {
        fun getResponse(chatFutures: ChatFutures, string: String, isResponse: Response) {
            val userMessage = Content.Builder().apply {
                role = "user"
                text(string)
            }.build()

            val executor = Executor { it.run() }

            val response: ListenableFuture<GenerateContentResponse> =
                chatFutures.sendMessage(userMessage)

            Futures.addCallback(response, object : FutureCallback<GenerateContentResponse> {
                override fun onSuccess(result: GenerateContentResponse?) {
                    result?.text?.let { isResponse.onResponse(it) }
                }

                override fun onFailure(t: Throwable) {
                    t.printStackTrace()
                    isResponse.onError(t)
                }
            }, executor)
        }
    }

    fun getModelAPI(): GenerativeModelFutures {
        val apiKey = "AIzaSyBMrnEs-e8RScUtQIN2is8TmrORxtgSeGM"

        val harassmentSafety = SafetySetting(
            HarmCategory.HARASSMENT,
            BlockThreshold.ONLY_HIGH
        )

        val generationConfig = GenerationConfig.Builder().apply {
            temperature = 0.9f
            topK = 16
            topP = 0.1f
        }.build()

        val model = GenerativeModel(
            "gemini-2.5-flash",
            apiKey,
            generationConfig,
            listOf(harassmentSafety)
        )

        return GenerativeModelFutures.from(model)
    }
}