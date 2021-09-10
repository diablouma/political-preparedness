package com.example.android.politicalpreparedness

import android.content.Context

class ResourceProvider {
    private var context: Context

    constructor(context: Context) {
        this.context = context
    }

    fun getStringArray(arrayId: Int): Array<out String> {
        return context.resources.getStringArray(arrayId)
    }
}