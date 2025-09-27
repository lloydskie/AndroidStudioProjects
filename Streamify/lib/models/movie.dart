class Movie {
  final int id;
  final String title;
  final String thumbnail;
  final String hlsUrl;
  final int duration; // Duration in seconds

  const Movie({
    required this.id,
    required this.title,
    required this.thumbnail,
    required this.hlsUrl,
    required this.duration,
  });

  // Create Movie from JSON
  factory Movie.fromJson(Map<String, dynamic> json) {
    return Movie(
      id: json['id'] as int,
      title: json['title'] as String,
      thumbnail: json['thumbnail'] as String,
      hlsUrl: json['hlsUrl'] as String,
      duration: json['duration'] as int,
    );
  }

  // Convert Movie to JSON
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'thumbnail': thumbnail,
      'hlsUrl': hlsUrl,
      'duration': duration,
    };
  }

  // Override equality operator for comparison
  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is Movie &&
        other.id == id &&
        other.title == title &&
        other.thumbnail == thumbnail &&
        other.hlsUrl == hlsUrl &&
        other.duration == duration;
  }

  // Override hashCode
  @override
  int get hashCode {
    return Object.hash(id, title, thumbnail, hlsUrl, duration);
  }

  // Override toString for debugging
  @override
  String toString() {
    return 'Movie(id: $id, title: $title, thumbnail: $thumbnail, hlsUrl: $hlsUrl, duration: $duration)';
  }

  // Create a copy of Movie with updated fields
  Movie copyWith({
    int? id,
    String? title,
    String? thumbnail,
    String? hlsUrl,
    int? duration,
  }) {
    return Movie(
      id: id ?? this.id,
      title: title ?? this.title,
      thumbnail: thumbnail ?? this.thumbnail,
      hlsUrl: hlsUrl ?? this.hlsUrl,
      duration: duration ?? this.duration,
    );
  }
}
