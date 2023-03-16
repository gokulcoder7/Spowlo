package com.bobbyesp.spowlo.ui.pages.searcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bobbyesp.spowlo.R
import com.bobbyesp.spowlo.ui.common.Route
import com.bobbyesp.spowlo.ui.components.HorizontalDivider
import com.bobbyesp.spowlo.ui.components.songs.search_feat.SearchingSongComponent
import com.bobbyesp.spowlo.ui.pages.metadata_viewer.binders.typeOfDataToString
import com.bobbyesp.spowlo.ui.pages.metadata_viewer.binders.typeOfSpotifyDataType
import com.bobbyesp.spowlo.ui.theme.harmonizeWithPrimary
import kotlinx.coroutines.delay

@Composable
fun SearcherPage(
    searcherPageViewModel: SearcherPageViewModel = hiltViewModel(),
    navController: NavController
) {
    val viewState by searcherPageViewModel.viewStateFlow.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SearcherPageImpl(
            viewState = viewState,
            onValueChange = { query ->
                searcherPageViewModel.updateSearchText(query)
            },
            onItemClick = { id -> navController.navigate(Route.PLAYLIST_PAGE + "/" + id) }
        )
    }
    LaunchedEffect(viewState.query) {
        if (viewState.query.isEmpty()) return@LaunchedEffect
        delay(300)
        searcherPageViewModel.makeSearch()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearcherPageImpl(
    viewState: SearcherPageViewModel.ViewState,
    onValueChange: (String) -> Unit,
    onItemClick: (String) -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) {
        with(viewState) {
            Column(modifier = Modifier.fillMaxSize()) {
                QueryTextBox(
                    modifier = Modifier.padding(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    query = query,
                    onValueChange = { query ->
                        onValueChange(query)
                    }
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    when (viewState.viewState) {
                        is ViewSearchState.Idle -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.background)
                                ) {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.search),
                                            modifier = Modifier.align(
                                                Alignment.CenterHorizontally
                                            ),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                }
                            }
                        }

                        is ViewSearchState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.background)
                                ) {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .padding(6.dp),
                                            strokeWidth = 4.dp
                                        )
                                        Text(
                                            text = stringResource(id = R.string.loading),
                                            modifier = Modifier.align(
                                                Alignment.CenterHorizontally
                                            ),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                }
                            }
                        }

                        is ViewSearchState.Error -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.background)
                                ) {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = stringResource(R.string.error),
                                            style = MaterialTheme.typography.headlineSmall,
                                            modifier = Modifier,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        is ViewSearchState.Success -> {
                            val allItems =
                                mutableListOf<Any>() //TODO: Add the filters. Pagination should be done in the future
                            viewState.viewState.data.let { data ->
                                data.albums?.items?.let { allItems.addAll(it) }
                                data.artists?.items?.let { allItems.addAll(it) }
                                data.playlists?.items?.let { allItems.addAll(it) }
                                data.tracks?.items?.let { allItems.addAll(it) }
                                data.episodes?.items?.let {
                                    allItems.addAll(
                                        listOf(
                                            it
                                        )
                                    )
                                }
                                data.shows?.items?.let {
                                    allItems.addAll(
                                        listOf(
                                            it
                                        )
                                    )
                                }
                                if (data != null) { //You may think that this is not necessary, but it is
                                    item {
                                        Text(
                                            text = stringResource(R.string.showing_results).format(
                                                allItems.size
                                            ),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .alpha(0.7f),
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Start,
                                            fontWeight = FontWeight.Bold
                                        )

                                    }
                                    data.albums?.items?.forEachIndexed { index, album ->
                                        item {
                                            // TODO: Display the album item
                                        }
                                    }
                                    data.artists?.items?.forEachIndexed { index, artist ->
                                        item {
                                            // TODO: Display the artist item
                                        }
                                    }

                                    data.tracks?.items?.forEachIndexed { index, track ->
                                        item {
                                            val artists: List<String> =
                                                track.artists.map { artist -> artist.name }
                                            SearchingSongComponent(
                                                artworkUrl = track.album.images[2].url,
                                                songName = track.name,
                                                artists = artists.joinToString(", "),
                                                spotifyUrl = track.externalUrls.spotify ?: "",
                                                onClick = { onItemClick(track.id) },
                                                type = typeOfDataToString(
                                                    type = typeOfSpotifyDataType(
                                                        track.type
                                                    )
                                                )
                                            )
                                            HorizontalDivider(
                                                modifier = Modifier.alpha(0.35f),
                                                color = MaterialTheme.colorScheme.primary.harmonizeWithPrimary()
                                            )
                                        }
                                    }

                                    data.playlists?.items?.forEachIndexed { index, playlist ->
                                        item {
                                            SearchingSongComponent(
                                                artworkUrl = playlist.images[0].url,
                                                songName = playlist.name,
                                                artists = playlist.owner.displayName
                                                    ?: stringResource(R.string.unknown),
                                                spotifyUrl = playlist.externalUrls.spotify ?: "",
                                                onClick = { onItemClick(playlist.id) },
                                                type = typeOfDataToString(
                                                    type = typeOfSpotifyDataType(
                                                        playlist.type
                                                    )
                                                )
                                            )
                                            HorizontalDivider(
                                                modifier = Modifier.alpha(0.35f),
                                                color = MaterialTheme.colorScheme.primary.harmonizeWithPrimary()
                                            )
                                        }
                                    }
                                    data.episodes?.items?.forEachIndexed { index, episode ->
                                        item {
                                            // TODO: Display the episode item
                                        }
                                    }
                                    data.shows?.items?.forEachIndexed { index, show ->
                                        item {
                                            // TODO: Display the show item
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QueryTextBox(
    modifier: Modifier = Modifier,
    query: String,
    onValueChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = onValueChange,
        placeholder = {
            if (query.isEmpty()) {
                Text(text = stringResource(id = R.string.searcher_page_query_text_box_label))
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                focusManager.clearFocus()
                softwareKeyboardController?.hide()
            }
        ),
        leadingIcon = {
            Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = null)
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                8.dp
            ), unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    )
}

enum class FilterType {
    ALL, ALBUMS, ARTISTS, PLAYLISTS, TRACKS, EPISODES, SHOWS
}
//TODO: Add filters
/*
*                                     val filterState = rememberSaveable { mutableStateOf(FilterType.ALL) }

                                    // Filter the items based on the selected filter type
                                    val filteredItems = when (filterState.value) {
                                        FilterType.ALL -> allItems
                                        FilterType.ALBUMS -> allItems.filterIsInstance<SimpleAlbum>()
                                        FilterType.ARTISTS -> allItems.filterIsInstance<Artist>()
                                        FilterType.PLAYLISTS -> allItems.filterIsInstance<SimplePlaylist>()
                                        FilterType.TRACKS -> allItems.filterIsInstance<Track>()
                                        FilterType.EPISODES -> allItems.filterIsInstance<SimpleEpisode>()
                                        FilterType.SHOWS -> allItems.filterIsInstance<SimpleShow>()
                                    }
* */