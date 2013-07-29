/*
 * Copyright (c) 2012, the Last.fm Java Project and Committers
 * All rights reserved.
 *
 * Redistribution and use of this software in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.paperairplane.music.share.utils.lastfm;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;

import com.paperairplane.music.share.utils.lastfm.MapUtilities;
import com.paperairplane.music.share.utils.lastfm.DomElement;

/**
 * Bean that contains information related to <code>Track</code>s and provides bindings to methods
 * in the <code>track.</code> namespace.
 *
 * @author Janni Kovacs
 */
public class Track extends MusicEntry {


	static final ItemFactory<Track> FACTORY = new TrackFactory();

	public static final String ARTIST_PAGE = "artistpage";
	public static final String ALBUM_PAGE = "albumpage";
	public static final String TRACK_PAGE = "trackpage";

	private String artist;
	private String artistMbid;

	protected String album;		// protected for use in Playlist.playlistFromElement
	private String albumMbid;
	private int position = -1;

	private boolean fullTrackAvailable;
	private boolean nowPlaying;

	private Date playedWhen;
	protected int duration;		// protected for use in Playlist.playlistFromElement
	protected String location;		// protected for use in Playlist.playlistFromElement

	protected Map<String, String> lastFmExtensionInfos = new HashMap<String, String>();		// protected for use in Playlist.playlistFromElement


	protected Track(String name, String url, String artist) {
		super(name, url);
		this.artist = artist;
	}

	protected Track(String name, String url, String mbid, int playcount, int listeners, boolean streamable,
					String artist, String artistMbid, boolean fullTrackAvailable, boolean nowPlaying) {
		super(name, url);
		this.artist = artist;
		this.artistMbid = artistMbid;
		this.fullTrackAvailable = fullTrackAvailable;
		this.nowPlaying = nowPlaying;
	}

	/**
	 * Returns the duration of the song, if available, in seconds. The duration attribute is only available
	 * for tracks retrieved by {@link Playlist#fetch(String, String) Playlist.fetch} and
	 * {@link Track#getInfo(String, String, String) Track.getInfo}.
	 *
	 * @return duration in seconds
	 */
	public int getDuration() {
		return duration;
	}

	public String getArtist() {
		return artist;
	}

	public String getArtistMbid() {
		return artistMbid;
	}

	public String getAlbum() {
		return album;
	}

	public String getAlbumMbid() {
		return albumMbid;
	}

	public boolean isFullTrackAvailable() {
		return fullTrackAvailable;
	}

	public boolean isNowPlaying() {
		return nowPlaying;
	}

	/**
	 * Returns the location (URL) of this Track. This information is only available with the {@link Radio} services.
	 *
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Returns last.fm specific information about this Track. Only available in Tracks fetched from
	 * radio playlists. <tt>key</tt> can be one of the following:
	 * <ul>
	 * <li>artistpage</li>
	 * <li>albumpage</li>
	 * <li>trackpage</li>
	 * <li>buyTrackURL</li>
	 * <li>buyAlbumURL</li>
	 * <li>freeTrackURL</li>
	 * </ul>
	 * Or use the available constants in this class.<br/>
	 * Note that the key string is case sensitive.
	 *
	 * @param key A key
	 * @return associated value
	 * @see #ARTIST_PAGE
	 * @see #ALBUM_PAGE
	 * @see #TRACK_PAGE
	 */
	public String getLastFmInfo(String key) {
		return lastFmExtensionInfos.get(key);
	}

	/**
	 * Returns the time when the track was played, if this data is available (e.g. for recent tracks) or <code>null</code>,
	 * if this data is not available.<br/>
	 *
	 * @return the date when the track was played or <code>null</code>
	 */
	public Date getPlayedWhen() {
		return playedWhen;
	}

	/**
	 * Returns the position of this track in its associated album, or -1 if not available.
	 *
	 * @return the album position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Searches for a track with the given name and returns a list of possible matches.
	 *
	 * @param track Track name
	 * @param apiKey The API key
	 * @return a list of possible matches
	 * @see #search(String, String, int, String)
	 */
	public static Collection<Track> search(String track, String apiKey, Context context) {
		return search(null, track, 30, apiKey, context);
	}

	/**
	 * Searches for a track with the given name and returns a list of possible matches.
	 * Specify an artist name or a limit to narrow down search results.
	 * Pass <code>null</code> for the artist parameter if you want to specify a limit but don't want
	 * to define an artist.
	 *
	 * @param artist Artist's name or <code>null</code>
	 * @param track Track name
	 * @param limit Number of maximum results
	 * @param apiKey The API key
	 * @return a list of possible matches
	 */
	public static Collection<Track> search(String artist, String track, int limit, String apiKey, Context context) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("track", track);
		params.put("limit", String.valueOf(limit));
		MapUtilities.nullSafePut(params, "artist", artist);
		Result result = Caller.getInstance(context).call("track.search", apiKey, params);
		if(!result.isSuccessful())
			return Collections.emptyList();
		DomElement element = result.getContentElement();
		DomElement matches = element.getChild("trackmatches");
		return ResponseBuilder.buildCollection(matches, Track.class);
	}



	/**
	 * Get the similar tracks for this track on Last.fm, based on listening data.<br/>
	 * You have to provide either an artist and a track name <i>or</i> an mbid. Pass <code>null</code>
	 * for parameters you don't need.
	 *
	 * @param artist The artist name in question
	 * @param trackOrMbid The track name in question or the track's MBID
	 * @param apiKey A Last.fm API key.
	 * @return a list of similar <code>Track</code>s
	 */
	public static Collection<Track> getSimilar(String artist, String trackOrMbid, String apiKey, Context context) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("artist", artist);
		params.put("track", trackOrMbid);
		Result result = Caller.getInstance(context).call("track.getSimilar", apiKey, params);
		return ResponseBuilder.buildCollection(result, Track.class);
	}


	/**
	 * Get the metadata for a track on Last.fm using the artist/track name or a musicbrainz id.
	 *
	 * @param artist The artist name in question or <code>null</code> if an mbid is specified
	 * @param trackOrMbid The track name in question or the musicbrainz id for the track
	 * @param apiKey A Last.fm API key.
	 * @return Track information
	 */
	public static Track getInfo(String artist, String trackOrMbid, String apiKey, Context context) {
		return getInfo(artist, trackOrMbid, null, null, apiKey, context);
	}

	/**
	 * Get the metadata for a track on Last.fm using the artist/track name or a musicbrainz id.
	 *
	 * @param artist The artist name in question or <code>null</code> if an mbid is specified
	 * @param trackOrMbid The track name in question or the musicbrainz id for the track
	 * @param locale The language to fetch info in, or <code>null</code>
	 * @param username The username for the context of the request, or <code>null</code>. If supplied, the user's playcount for this track and whether they have loved the track is included in the response
	 * @param apiKey A Last.fm API key.
	 * @return Track information
	 */
	public static Track getInfo(String artist, String trackOrMbid, Locale locale, String username, String apiKey, Context context) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("artist", artist);
		params.put("track", trackOrMbid);
		if (locale != null && locale.getLanguage().length() != 0) {
			params.put("lang", locale.getLanguage());
		}
		MapUtilities.nullSafePut(params, "username", username);
		Result result = Caller.getInstance(context).call("track.getInfo", apiKey, params);
		if (!result.isSuccessful())
			return null;
		DomElement content = result.getContentElement();
		DomElement album = content.getChild("album");
		Track track = FACTORY.createItemFromElement(content);
		if (album != null) {
			String pos = album.getAttribute("position");
			if ((pos != null) && pos.length() != 0) {
				track.position = Integer.parseInt(pos);
			}
			track.album = album.getChildText("title");
			track.albumMbid = album.getChildText("mbid");
			ImageHolder.loadImages(track, album);
		}
		return track;
	}

	/**
	 * Use the last.fm corrections data to check whether the supplied track has a correction to a canonical track. This method returns a new
	 * {@link Track} object containing the corrected data, or <code>null</code> if the supplied Artist/Track combination was not found.
	 *
	 * @param artist The artist name to correct
	 * @param track The track name to correct
	 * @param apiKey A Last.fm API key
	 * @return a new {@link Track}, or <code>null</code>
	 */
	public static Track getCorrection(String artist, String track, String apiKey, Context context) {
		Result result = Caller.getInstance(context).call("track.getCorrection", apiKey, "artist", artist, "track", track);
		if (!result.isSuccessful())
			return null;
		DomElement correctionElement = result.getContentElement().getChild("correction");
		if (correctionElement == null)
			return new Track(track, null, artist);
		DomElement trackElem = correctionElement.getChild("track");
		return FACTORY.createItemFromElement(trackElem);
	}

	@Override
	public String toString() {
		return "Track[name=" + name + ",artist=" + artist + ", album=" + album + ", position=" + position + ", duration=" + duration
				+ ", location=" + location + ", nowPlaying=" + nowPlaying + ", fullTrackAvailable=" + fullTrackAvailable + ", playedWhen="
				+ playedWhen + ", artistMbId=" + artistMbid + ", albumMbId" + albumMbid + "]";
	}

	private static class TrackFactory implements ItemFactory<Track> {
		public Track createItemFromElement(DomElement element) {
			Track track = new Track(null, null, null);
			MusicEntry.loadStandardInfo(track, element);
			final String nowPlayingAttr = element.getAttribute("nowplaying");
			if (nowPlayingAttr != null)
				track.nowPlaying = Boolean.valueOf(nowPlayingAttr);
			if (element.hasChild("duration")) {
				String duration = element.getChildText("duration");
				if(duration.length() != 0) {
					int durationLength = Integer.parseInt(duration);
					// So it seems last.fm couldn't decide which format to send the duration in.
					// It's supplied in milliseconds for Playlist.fetch and Track.getInfo but Artist.getTopTracks returns (much saner) seconds
					// so we're doing a little sanity check for the duration to be over or under 10'000 and decide what to do
					track.duration = durationLength > 10000 ? durationLength / 1000 : durationLength;
				}
			}
			DomElement album = element.getChild("album");
			if (album != null) {
				track.album = album.getText();
				track.albumMbid = album.getAttribute("mbid");
			}
			DomElement artist = element.getChild("artist");
			if (artist.getChild("name") != null) {
				track.artist = artist.getChildText("name");
				track.artistMbid = artist.getChildText("mbid");
			} else {
				track.artist = artist.getText();
				track.artistMbid = artist.getAttribute("mbid");
			}
			DomElement date = element.getChild("date");
			if (date != null) {
				String uts = date.getAttribute("uts");
				long utsTime = Long.parseLong(uts);
				track.playedWhen = new Date(utsTime * 1000);
			}
			DomElement stream = element.getChild("streamable");
			if (stream != null) {
				String s = stream.getAttribute("fulltrack");
				track.fullTrackAvailable = s != null && Integer.parseInt(s) == 1;
			}
			return track;
		}
	}
}
