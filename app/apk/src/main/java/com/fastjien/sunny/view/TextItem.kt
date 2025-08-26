package com.fastjien.sunny.view

import com.fastjien.sunny.R
import com.fastjien.sunny.databinding.DiffItem
import com.fastjien.sunny.databinding.ItemWrapper
import com.fastjien.sunny.databinding.RvItem

class TextItem(override val item: Int) : RvItem(), DiffItem<TextItem>, ItemWrapper<Int> {
    override val layoutRes = R.layout.item_text
}
