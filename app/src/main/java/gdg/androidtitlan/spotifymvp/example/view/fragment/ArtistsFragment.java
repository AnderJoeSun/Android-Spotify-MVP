/**
 * Copyright 2015 Erik Jhordan Rey.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gdg.androidtitlan.spotifymvp.example.view.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import gdg.androidtitlan.spotifymvp.R;
import gdg.androidtitlan.spotifymvp.example.data.api.client.SpotifyClient;
import gdg.androidtitlan.spotifymvp.example.data.model.Artist;
import gdg.androidtitlan.spotifymvp.example.interactor.ArtistsInteractor;
import gdg.androidtitlan.spotifymvp.example.presenter.ArtistsPresenter;
import gdg.androidtitlan.spotifymvp.example.view.activity.TracksActivity;
import gdg.androidtitlan.spotifymvp.example.view.adapter.ArtistsAdapter;

public class ArtistsFragment extends Fragment
    implements ArtistsPresenter.View, SearchView.OnQueryTextListener {

  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.rv_artists)
  RecyclerView rv_artist;
  @BindView(R.id.pv_artists) ProgressBar pv_artists;
  @BindView(R.id.iv_artists) ImageView iv_artists;
  @BindView(R.id.txt_line_artists) TextView txt_line_artists;
  @BindView(R.id.txt_subline_artists) TextView txt_sub_line_artists;

  private ArtistsPresenter artistsPresenter;

  public ArtistsFragment() {
    setHasOptionsMenu(true);
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    artistsPresenter = new ArtistsPresenter(new ArtistsInteractor(new SpotifyClient()));
    artistsPresenter.setView(this);
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_artists, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);
    setupToolbar();
    setupRecyclerView();
  }

  @Override public void onDestroy() {
    artistsPresenter.terminate();
    super.onDestroy();
  }

  @Override public Context getContext() {
    return getActivity();
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_music, menu);
    setupSearchView(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_github) {
      startActivityActionView();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public boolean onQueryTextSubmit(String query) {
    artistsPresenter.onSearchArtist(query);
    return true;
  }

  @Override public boolean onQueryTextChange(String newText) {
    return true;
  }

  @Override public void showLoading() {
    pv_artists.setVisibility(View.VISIBLE);
    iv_artists.setVisibility(View.GONE);
    txt_line_artists.setVisibility(View.GONE);
    txt_sub_line_artists.setVisibility(View.GONE);
    rv_artist.setVisibility(View.GONE);
  }

  @Override public void hideLoading() {
    pv_artists.setVisibility(View.GONE);
    rv_artist.setVisibility(View.VISIBLE);
  }

  @Override public void showArtistNotFoundMessage() {
    pv_artists.setVisibility(View.GONE);
    txt_line_artists.setVisibility(View.VISIBLE);
    iv_artists.setVisibility(View.VISIBLE);
    txt_line_artists.setText(getString(R.string.error_artist_not_found));
    iv_artists.setImageDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.ic_not_found));
  }

  @Override public void showConnectionErrorMessage() {
    pv_artists.setVisibility(View.GONE);
    txt_line_artists.setVisibility(View.VISIBLE);
    iv_artists.setVisibility(View.VISIBLE);
    txt_line_artists.setText(getString(R.string.error_internet_connection));
    iv_artists.setImageDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.ic_not_internet));
  }

  @Override public void showServerError() {
    pv_artists.setVisibility(View.GONE);
    txt_line_artists.setVisibility(View.VISIBLE);
    iv_artists.setVisibility(View.VISIBLE);
    txt_line_artists.setText(getString(R.string.error_server_internal));
    iv_artists.setImageDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.ic_not_found));
  }

  @Override public void renderArtists(List<Artist> artists) {
    ArtistsAdapter adapter = (ArtistsAdapter) rv_artist.getAdapter();
    adapter.setArtists(artists);
    adapter.notifyDataSetChanged();
  }

  private void setupSearchView(Menu menu) {
    SearchManager searchManager =
        (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
    searchView.setQueryHint(getString(R.string.search_hint));
    searchView.setMaxWidth(toolbar.getWidth());
    searchView.setOnQueryTextListener(this);
  }

  private void setupToolbar() {
    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowTitleEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeAsUpIndicator(R.mipmap.ic_action_navigation_menu);
    }
  }

  private void setupRecyclerView() {
    ArtistsAdapter adapter = new ArtistsAdapter();
    adapter.setItemClickListener(
        (Artist artist, int position) -> artistsPresenter.launchArtistDetail(artist));
    rv_artist.setAdapter(adapter);
  }

  @Override public void launchArtistDetail(Artist artist) {
    Intent intent = new Intent(getContext(), TracksActivity.class);
    intent.putExtra(TracksActivity.EXTRA_REPOSITORY, artist);
    startActivity(intent);
  }

  private void startActivityActionView() {
    startActivity(new Intent(Intent.ACTION_VIEW,
        Uri.parse("https://github.com/erikcaffrey/Android-Spotify-MVP")));
  }

  @Override public Context context() {
    return null;
  }
}
