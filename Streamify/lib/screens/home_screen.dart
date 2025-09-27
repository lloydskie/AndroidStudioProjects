import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import '../models/movie.dart';
import 'video_player_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  Future<List<Movie>> _fetchMovies() async {
    try {
      // Mock API endpoint - replace with your actual API
      final response = await http.get(
        Uri.parse('https://jsonplaceholder.typicode.com/posts'), // Mock endpoint
        headers: {'Content-Type': 'application/json'},
      );

      if (response.statusCode == 200) {
        // For demo purposes, creating mock movie data
        // Replace this with actual API response parsing
        return _generateMockMovies();
      } else {
        throw Exception('Failed to load movies');
      }
    } catch (e) {
      // Return mock data on error for development
      return _generateMockMovies();
    }
  }

  List<Movie> _generateMockMovies() {
    return [
      // Trending movies - using working sample video URLs
      const Movie(
        id: 1,
        title: 'The Matrix',
        thumbnail: 'https://via.placeholder.com/300x450/FF5722/FFFFFF?text=The+Matrix',
        hlsUrl: 'https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8',
        duration: 8160,
      ),
      const Movie(
        id: 2,
        title: 'Inception',
        thumbnail: 'https://via.placeholder.com/300x450/2196F3/FFFFFF?text=Inception',
        hlsUrl: 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
        duration: 8880,
      ),
      const Movie(
        id: 3,
        title: 'Interstellar',
        thumbnail: 'https://via.placeholder.com/300x450/4CAF50/FFFFFF?text=Interstellar',
        hlsUrl: 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4',
        duration: 10140,
      ),
      // New movies
      const Movie(
        id: 4,
        title: 'Dune: Part Two',
        thumbnail: 'https://via.placeholder.com/300x450/FF9800/FFFFFF?text=Dune+2',
        hlsUrl: 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
        duration: 9960,
      ),
      const Movie(
        id: 5,
        title: 'Oppenheimer',
        thumbnail: 'https://via.placeholder.com/300x450/795548/FFFFFF?text=Oppenheimer',
        hlsUrl: 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4',
        duration: 10800,
      ),
      const Movie(
        id: 6,
        title: 'Spider-Verse',
        thumbnail: 'https://via.placeholder.com/300x450/E91E63/FFFFFF?text=Spider+Verse',
        hlsUrl: 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4',
        duration: 7020,
      ),
      // Recommended movies
      const Movie(
        id: 7,
        title: 'Avatar',
        thumbnail: 'https://via.placeholder.com/300x450/00BCD4/FFFFFF?text=Avatar',
        hlsUrl: 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4',
        duration: 9720,
      ),
      const Movie(
        id: 8,
        title: 'Blade Runner 2049',
        thumbnail: 'https://via.placeholder.com/300x450/9C27B0/FFFFFF?text=Blade+Runner',
        hlsUrl: 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4',
        duration: 9840,
      ),
      const Movie(
        id: 9,
        title: 'The Dark Knight',
        thumbnail: 'https://via.placeholder.com/300x450/607D8B/FFFFFF?text=Dark+Knight',
        hlsUrl: 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4',
        duration: 9120,
      ),
    ];
  }

  void _navigateToVideoPlayer(BuildContext context, String hlsUrl, String title) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => VideoPlayerScreen(
          url: hlsUrl,
          title: title,
        ),
      ),
    );
  }

  Widget _buildMovieCarousel(String title, List<Movie> movies, BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
          child: Text(
            title,
            style: const TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
        ),
        SizedBox(
          height: 280,
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            itemCount: movies.length,
            itemBuilder: (context, index) {
              final movie = movies[index];
              return _buildMovieCard(movie, context);
            },
          ),
        ),
      ],
    );
  }

  Widget _buildMovieCard(Movie movie, BuildContext context) {
    return Container(
      width: 160,
      margin: const EdgeInsets.only(right: 12.0),
      child: GestureDetector(
        onTap: () => _navigateToVideoPlayer(context, movie.hlsUrl, movie.title),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              height: 220,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(12),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: 0.3),
                    blurRadius: 8,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: Stack(
                  fit: StackFit.expand,
                  children: [
                    Image.network(
                      movie.thumbnail,
                      fit: BoxFit.cover,
                      loadingBuilder: (context, child, loadingProgress) {
                        if (loadingProgress == null) return child;
                        return Container(
                          color: Colors.grey[800],
                          child: const Center(
                            child: CircularProgressIndicator(
                              color: Colors.deepPurple,
                            ),
                          ),
                        );
                      },
                      errorBuilder: (context, error, stackTrace) {
                        return Container(
                          color: Colors.grey[800],
                          child: const Icon(
                            Icons.movie,
                            color: Colors.white54,
                            size: 50,
                          ),
                        );
                      },
                    ),
                    // Gradient overlay for title readability
                    Positioned(
                      bottom: 0,
                      left: 0,
                      right: 0,
                      child: Container(
                        height: 80,
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            begin: Alignment.topCenter,
                            end: Alignment.bottomCenter,
                            colors: [
                              Colors.transparent,
                              Colors.black.withValues(alpha: 0.8),
                            ],
                          ),
                        ),
                        padding: const EdgeInsets.all(12.0),
                        child: Align(
                          alignment: Alignment.bottomLeft,
                          child: Text(
                            movie.title,
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 14,
                              fontWeight: FontWeight.bold,
                              shadows: [
                                Shadow(
                                  color: Colors.black,
                                  offset: Offset(1, 1),
                                  blurRadius: 2,
                                ),
                              ],
                            ),
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                      ),
                    ),
                    // Play icon overlay
                    Positioned.fill(
                      child: Container(
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(12),
                          color: Colors.black.withValues(alpha: 0.0),
                        ),
                        child: const Icon(
                          Icons.play_circle_outline,
                          color: Colors.white70,
                          size: 40,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              _formatDuration(movie.duration),
              style: TextStyle(
                color: Colors.grey[400],
                fontSize: 12,
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _formatDuration(int seconds) {
    final hours = seconds ~/ 3600;
    final minutes = (seconds % 3600) ~/ 60;

    if (hours > 0) {
      return '${hours}h ${minutes}m';
    } else {
      return '${minutes}m';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: const Text(
          'Streamify',
          style: TextStyle(
            fontSize: 28,
            fontWeight: FontWeight.bold,
            color: Colors.deepPurple,
          ),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.search, color: Colors.white),
            onPressed: () {
              // TODO: Implement search functionality
            },
          ),
          IconButton(
            icon: const Icon(Icons.person, color: Colors.white),
            onPressed: () {
              // TODO: Implement profile/settings
            },
          ),
        ],
      ),
      body: FutureBuilder<List<Movie>>(
        future: _fetchMovies(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(
              child: CircularProgressIndicator(
                color: Colors.deepPurple,
              ),
            );
          }

          if (snapshot.hasError) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(
                    Icons.error_outline,
                    color: Colors.red,
                    size: 60,
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'Failed to load movies',
                    style: TextStyle(
                      color: Colors.grey[400],
                      fontSize: 18,
                    ),
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () {
                      // Trigger rebuild to retry
                      (context as Element).markNeedsBuild();
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.deepPurple,
                    ),
                    child: const Text('Retry'),
                  ),
                ],
              ),
            );
          }

          if (!snapshot.hasData || snapshot.data!.isEmpty) {
            return Center(
              child: Text(
                'No movies available',
                style: TextStyle(
                  color: Colors.grey[400],
                  fontSize: 18,
                ),
              ),
            );
          }

          final movies = snapshot.data!;
          final trendingMovies = movies.take(3).toList();
          final newMovies = movies.skip(3).take(3).toList();
          final recommendedMovies = movies.skip(6).take(3).toList();

          return SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SizedBox(height: 20),
                _buildMovieCarousel('Trending', trendingMovies, context),
                const SizedBox(height: 32),
                _buildMovieCarousel('New Releases', newMovies, context),
                const SizedBox(height: 32),
                _buildMovieCarousel('Recommended', recommendedMovies, context),
                const SizedBox(height: 32),
              ],
            ),
          );
        },
      ),
    );
  }
}
