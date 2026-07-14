// Mövzu (topic) adları bazada yalnız Azərbaycan dilində saxlanılır (sual mətnlərindən fərqli
// olaraq ayrıca tərcümə cədvəli yoxdur — mövzu siyahısı sabit və kiçikdir). Türkçə interfeys üçün
// göstərilən adı burada tərcümə edirik; backend-ə göndərilən/açar kimi istifadə olunan dəyər
// dəyişmir.

import { getLang } from './strings';

const topicTranslationsTr: Record<string, string> = {
  'API Dizaynı və Sənədləşmə': 'API Tasarımı ve Dokümantasyon',
  'Agile və Scrum': 'Agile ve Scrum',
  'Alqoritmlər və Mürəkkəblik': 'Algoritmalar ve Karmaşıklık',
  'Annotations və Reflection': 'Annotations ve Reflection',
  'Autentifikasiya və Avtorizasiya': 'Kimlik Doğrulama ve Yetkilendirme',
  'Axtarış Alqoritmləri və Ağaclar': 'Arama Algoritmaları ve Ağaçlar',
  'CI/CD və DevOps': 'CI/CD ve DevOps',
  'Concurrency Alətləri': 'Concurrency Araçları',
  'Dizayn Şablonları': 'Tasarım Kalıpları',
  'Docker və Konteynerləşdirmə': 'Docker ve Konteynerleştirme',
  'Enum-lar': "Enum'lar",
  'Functional Programming Konsepsiyaları': 'Functional Programming Kavramları',
  Generics: 'Generics',
  'Git və Versiya Nəzarəti': 'Git ve Versiyon Kontrolü',
  'I/O və Serialization': 'I/O ve Serialization',
  'Immutability Dizaynı': 'Immutability Tasarımı',
  'JDBC və Verilənlər Bazası': 'JDBC ve Veritabanı',
  'JVM Performans Tənzimləmə': 'JVM Performans Ayarlama',
  'JVM və Yaddaş': 'JVM ve Bellek',
  'Java 8+ Xüsusiyyətləri': 'Java 8+ Özellikleri',
  'Keşləmə Strategiyaları': 'Önbellekleme Stratejileri',
  'Kod Keyfiyyəti və Clean Code': 'Kod Kalitesi ve Clean Code',
  Kolleksiyalar: 'Koleksiyonlar',
  'Kubernetes Əsasları': 'Kubernetes Temelleri',
  'Logging və Monitorinq': 'Logging ve İzleme',
  'Maven və Gradle': 'Maven ve Gradle',
  'Mesaj Növbələri': 'Mesaj Kuyrukları',
  'Mikroservislər Memarlığı': 'Mikroservis Mimarisi',
  Multithreading: 'Multithreading',
  'Networking və HTTP Əsasları': 'Ağ ve HTTP Temelleri',
  'NoSQL Verilənlər Bazaları': 'NoSQL Veritabanları',
  OOP: 'OOP',
  'Object Sinifinin Metodları': 'Object Sınıfının Metotları',
  'Proqram Memarlığı Üslubları': 'Yazılım Mimarisi Stilleri',
  'REST API və Veb Servislər': 'REST API ve Web Servisleri',
  'SOLID Prinsipləri': 'SOLID Prensipleri',
  'SQL və Verilənlər Bazası Dizaynı': 'SQL ve Veritabanı Tasarımı',
  'Serialization Formatları': 'Serialization Formatları',
  'Sistem Dizaynı Əsasları': 'Sistem Tasarımı Temelleri',
  'Spring Framework Əsasları': 'Spring Framework Temelleri',
  'Stream və Lambda': 'Stream ve Lambda',
  String: 'String',
  'Sıralama Alqoritmləri': 'Sıralama Algoritmaları',
  Testing: 'Testing',
  Təhlükəsizlik: 'Güvenlik',
  'Verilənlər Bazası İndeksləmə və Optimallaşdırma': 'Veritabanı İndeksleme ve Optimizasyon',
  'Verilənlər Strukturları': 'Veri Yapıları',
  'WebSocket və Real-vaxt Kommunikasiya': 'WebSocket ve Gerçek Zamanlı İletişim',
  'Xəta Loglama və Debugging Texnikaları': 'Hata Loglama ve Debugging Teknikleri',
  İstisnalar: 'İstisnalar (Exceptions)'
};

export function translateTopic(topic: string): string {
  return getLang() === 'tr' ? (topicTranslationsTr[topic] ?? topic) : topic;
}
