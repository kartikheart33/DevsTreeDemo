package com.devstreepractical.util

interface OnSavedItemClick<T> {
    fun onTap(model: T,position:Int)
    fun onDelete(model: T,position:Int)
    fun onEdit(model: T,position:Int)
}