import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:better_player/better_player.dart';

class VideoPlayerScreen extends StatefulWidget {
  final String url;
  final String? title;

  const VideoPlayerScreen({
    super.key,
    required this.url,
    this.title,
  });

  @override
  State<VideoPlayerScreen> createState() => _VideoPlayerScreenState();
}

class _VideoPlayerScreenState extends State<VideoPlayerScreen> {
  BetterPlayerController? _betterPlayerController;
  bool _isPlayerReady = false;
  bool _isFullscreen = false;

  @override
  void initState() {
    super.initState();
    _initializePlayer();
  }

  void _initializePlayer() {
    // Configure better player
    BetterPlayerConfiguration betterPlayerConfiguration = BetterPlayerConfiguration(
      aspectRatio: 16 / 9,
      autoPlay: true,
      looping: false,
      fullScreenByDefault: false,
      allowedScreenSleep: false,
      deviceOrientationsOnFullScreen: [
        DeviceOrientation.landscapeLeft,
        DeviceOrientation.landscapeRight,
      ],
      deviceOrientationsAfterFullScreen: [
        DeviceOrientation.portraitUp,
      ],
      systemOverlaysAfterFullScreen: SystemUiOverlay.values,
      controlsConfiguration: const BetterPlayerControlsConfiguration(
        controlBarColor: Colors.black54,
        iconsColor: Colors.white,
        playIcon: Icons.play_arrow,
        pauseIcon: Icons.pause,
        muteIcon: Icons.volume_off,
        unMuteIcon: Icons.volume_up,
        fullscreenEnableIcon: Icons.fullscreen,
        fullscreenDisableIcon: Icons.fullscreen_exit,
        progressBarPlayedColor: Colors.deepPurple,
        progressBarHandleColor: Colors.deepPurpleAccent,
        progressBarBufferedColor: Colors.grey,
        progressBarBackgroundColor: Colors.black26,
        textColor: Colors.white,
        controlBarHeight: 40,
        loadingColor: Colors.deepPurple,
        overflowMenuIconsColor: Colors.white,
        enableFullscreen: true,
        enableMute: true,
        enableProgressText: true,
        enableProgressBar: true,
        enablePlayPause: true,
        enableOverflowMenu: true,
        showControlsOnInitialize: false,
      ),
    );

    // Determine data source type based on URL extension
    BetterPlayerDataSource dataSource;
    if (widget.url.contains('.m3u8')) {
      // HLS stream
      dataSource = BetterPlayerDataSource(
        BetterPlayerDataSourceType.network,
        widget.url,
        liveStream: false,
        useAsmsSubtitles: true,
        videoFormat: BetterPlayerVideoFormat.hls,
      );
    } else {
      // Regular MP4 or other formats
      dataSource = BetterPlayerDataSource(
        BetterPlayerDataSourceType.network,
        widget.url,
        videoFormat: BetterPlayerVideoFormat.other,
      );
    }

    _betterPlayerController = BetterPlayerController(
      betterPlayerConfiguration,
      betterPlayerDataSource: dataSource,
    );

    // Listen for player events
    _betterPlayerController!.addEventsListener((BetterPlayerEvent event) {
      if (event.betterPlayerEventType == BetterPlayerEventType.initialized) {
        setState(() {
          _isPlayerReady = true;
        });
      }
      // Note: Better Player handles fullscreen state internally
      // We'll track it through the controller's isFullScreen property
    });
  }

  @override
  void dispose() {
    _betterPlayerController?.dispose();
    super.dispose();
  }

  void _toggleFullscreen() {
    if (_betterPlayerController != null) {
      if (_isFullscreen) {
        _betterPlayerController!.exitFullScreen();
      } else {
        _betterPlayerController!.enterFullScreen();
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: _isFullscreen
          ? null
          : AppBar(
              backgroundColor: Colors.black,
              elevation: 0,
              leading: IconButton(
                icon: const Icon(Icons.arrow_back, color: Colors.white),
                onPressed: () => Navigator.of(context).pop(),
              ),
              title: Text(
                widget.title ?? 'Video Player',
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 18,
                  fontWeight: FontWeight.w500,
                ),
              ),
              actions: [
                IconButton(
                  icon: Icon(
                    _isFullscreen ? Icons.fullscreen_exit : Icons.fullscreen,
                    color: Colors.white,
                  ),
                  onPressed: _toggleFullscreen,
                ),
              ],
            ),
      body: _buildVideoPlayer(),
    );
  }

  Widget _buildVideoPlayer() {
    if (_betterPlayerController == null) {
      return const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            CircularProgressIndicator(
              color: Colors.deepPurple,
            ),
            SizedBox(height: 16),
            Text(
              'Initializing player...',
              style: TextStyle(
                color: Colors.white,
                fontSize: 16,
              ),
            ),
          ],
        ),
      );
    }

    return Center(
      child: AspectRatio(
        aspectRatio: _isFullscreen ? MediaQuery.of(context).size.aspectRatio : 16 / 9,
        child: Container(
          color: Colors.black,
          child: Stack(
            children: [
              BetterPlayer(controller: _betterPlayerController!),
              if (!_isPlayerReady)
                Container(
                  color: Colors.black,
                  child: const Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        CircularProgressIndicator(
                          color: Colors.deepPurple,
                          strokeWidth: 3,
                        ),
                        SizedBox(height: 16),
                        Text(
                          'Loading video...',
                          style: TextStyle(
                            color: Colors.white70,
                            fontSize: 14,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

// Extension to help with video format detection
extension VideoUrlExtension on String {
  bool get isHLS => contains('.m3u8');
  bool get isMP4 => contains('.mp4');
  bool get isVideoUrl => isHLS || isMP4 || contains('.mov') || contains('.avi') || contains('.mkv');
}
