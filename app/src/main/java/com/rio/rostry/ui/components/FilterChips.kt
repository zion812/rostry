package com.rio.rostry.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rio.rostry.data.model.FowlType

/**
 * Filter chips for fowl filtering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FowlFilterChips(
    selectedFilters: Set<FowlFilter>,
    onFilterChange: (FowlFilter, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val availableFilters = remember {
        listOf(
            FowlFilter.ForSale,
            FowlFilter.Breeders,
            FowlFilter.Chickens,
            FowlFilter.Ducks,
            FowlFilter.Turkeys,
            FowlFilter.Geese,
            FowlFilter.GuineaFowl
        )
    }
    
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(availableFilters) { filter ->
            FilterChip(
                selected = filter in selectedFilters,
                onClick = { 
                    onFilterChange(filter, filter !in selectedFilters)
                },
                label = { 
                    Text(
                        text = filter.displayName,
                        style = MaterialTheme.typography.labelMedium
                    ) 
                },
                leadingIcon = if (filter in selectedFilters) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
        
        // Clear all filters chip
        if (selectedFilters.isNotEmpty()) {
            item {
                AssistChip(
                    onClick = { 
                        selectedFilters.forEach { filter ->
                            onFilterChange(filter, false)
                        }
                    },
                    label = { Text("Clear All") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
        }
    }
}

/**
 * Filter data class
 */
sealed class FowlFilter(val displayName: String) {
    object ForSale : FowlFilter("For Sale")
    object Breeders : FowlFilter("Breeders")
    object Chickens : FowlFilter("Chickens")
    object Ducks : FowlFilter("Ducks")
    object Turkeys : FowlFilter("Turkeys")
    object Geese : FowlFilter("Geese")
    object GuineaFowl : FowlFilter("Guinea Fowl")
}

/**
 * Price range filter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceRangeFilter(
    minPrice: Float,
    maxPrice: Float,
    onPriceRangeChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
    priceRange: ClosedFloatingPointRange<Float> = 0f..10000f
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Price Range",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₱${minPrice.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "₱${maxPrice.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            RangeSlider(
                value = minPrice..maxPrice,
                onValueChange = { range ->
                    onPriceRangeChange(range.start, range.endInclusive)
                },
                valueRange = priceRange,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Sort options component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortOptions(
    selectedSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortOptions = remember {
        listOf(
            SortOption.DateAdded,
            SortOption.PriceLowToHigh,
            SortOption.PriceHighToLow,
            SortOption.Name,
            SortOption.Breed
        )
    }
    
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(sortOptions) { option ->
            FilterChip(
                selected = option == selectedSort,
                onClick = { onSortChange(option) },
                label = { 
                    Text(
                        text = option.displayName,
                        style = MaterialTheme.typography.labelMedium
                    ) 
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

/**
 * Sort option data class
 */
sealed class SortOption(val displayName: String) {
    object DateAdded : SortOption("Date Added")
    object PriceLowToHigh : SortOption("Price: Low to High")
    object PriceHighToLow : SortOption("Price: High to Low")
    object Name : SortOption("Name")
    object Breed : SortOption("Breed")
}