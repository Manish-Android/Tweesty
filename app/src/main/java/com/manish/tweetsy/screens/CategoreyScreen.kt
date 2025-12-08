package com.manish.tweetsy.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.manish.tweetsy.R
import com.manish.tweetsy.screens.commonScreens.DotLoader
import com.manish.tweetsy.viewmodel.CategoryViewModel

@Composable
fun CategoryScreen(onClick: (category: String) -> Unit) {
    val categoryViewModel: CategoryViewModel = hiltViewModel()
    val categories = categoryViewModel.categories.collectAsState()

    if (categories.value.isNullOrEmpty()) {
        DotLoader()
    }
    else
    {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.SpaceAround,
        ) {
            items(categories.value!!.size) {
                val categoryName = categories.value!![it]
                CategoryItem(category = categoryName, onClick)


            }
        }
    }


}

@Composable
fun CategoryItem(category: String, onClick: (category: String) -> Unit) {

    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(160.dp)
            .clip(RoundedCornerShape(10.dp))
            .paint(
                painter = painterResource(R.drawable.bg),
                contentScale = ContentScale.Crop
            )
            .clickable {
                onClick(category)
            }
            .border(
                2.dp,
                Color(0xFFEEEEEE),
                RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            text = category,
            fontSize = 18.sp,
            modifier = Modifier.padding(0.dp, 20.dp),
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium
        )
    }

}

