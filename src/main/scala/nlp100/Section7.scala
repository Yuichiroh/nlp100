package nlp100

import scala.io.{Source, StdIn}

/** Code for 言語処理100本ノック 2015 第7章: データベース
  * http://www.cl.ecei.tohoku.ac.jp/nlp100/
  * @author Yuichiroh Matsubayashi
  *         Created on 15/02/10.
  */

/** artist.json.gzは，オープンな音楽データベースMusicBrainzの中で，アーティストに関するものをJSON形式に変換し，gzip形式で圧縮したファイルである．
  * このファイルには，1アーティストに関する情報が1行にJSON形式で格納されている．JSON形式の概要は以下の通りである． 
  *
  * フィールド	型	内容	例
  * id	ユニーク識別子	整数	20660
  * gid	グローバル識別子	文字列	"ecf9f3a3-35e9-4c58-acaa-e707fba45060"
  * name	アーティスト名	文字列	"Oasis"
  * sort_name	アーティスト名（辞書順整列用）	文字列	"Oasis"
  * area	活動場所	文字列	"United Kingdom"
  * aliases	別名	辞書オブジェクトのリスト
  * .name	別名	文字列	"オアシス"
  * .sort_name	別名（整列用）	文字列	"オアシス"
  * begin	活動開始日	辞書
  * .year	活動開始年	整数	1991
  * .month	活動開始月	整数
  * .date	活動開始日	整数
  * begin	活動開始日	辞書
  * .year	活動終了年	整数	2009
  * .month	活動終了月	整数	8
  * .date	活動終了日	整数	28
  * tags	タグ	辞書オブジェクトのリスト
  * .count	タグ付けされた数	整数	1
  * .value	タグ内容	文字列	"rock"
  * rating	レーティング	辞書オブジェクトのリスト
  * .count	レーティングの投票数	整数	13
  * .value	レーティングの値	整数	86
  *
  * artist.json.gzのデータをKey-Value-Store (KVS) およびドキュメント志向型データベースに格納・検索することを考える．
  * KVSとしては，LevelDB，Redis，KyotoCabinet等を用いよ．ドキュメント志向型データベースとしては，MongoDB，CouchDB，RethinkDB等を用いよ． */

/** common methods/fields for section 7 */
object Section7 {
}

/** Key-Value-Store (KVS) を用い，アーティスト名（name）から活動場所（area）を検索するためのデータベースを構築せよ． */
object P60 extends App {

  import com.redis.RedisClient
  import org.json4s._
  import org.json4s.native.JsonMethods._

  val r = new RedisClient("localhost", 6379)
  r.flushall

  Source.stdin.getLines().foreach { line =>
    val artist = parse(line)
    artist \ "name" match {
      case JString(name) =>
        artist \ "area" match {
          case JString(area) => r.set(name, area)
          case _ =>
        }
      case _ =>
    }
  }
}

/** 60で構築したデータベースを用い，特定の（指定した）アーティストの活動場所を取得せよ． */
object P61 extends App {

  import com.redis.RedisClient

  val r = new RedisClient("localhost", 6379)
  println(r.get(StdIn.readLine()))
}

/** 60で構築したデータベースを用い，活動場所が「Japan」となっているアーティスト数を求めよ． */
object P62 extends App {

  import com.redis.RedisClient

  val r = new RedisClient("localhost", 6379)
  val japan = r.keys().get.count(k => r.get(k.get).contains("Japan"))

  println(japan)
}

/** KVSを用い，アーティストの別名（aliases.value）からアーティスト名（name）のリストを検索するためのデータベースを構築せよ．
  * さらに，ここで構築したデータベースを用い，特定の（指定した）別名を持つアーティストを検索せよ． */
object P63 extends App {

  import com.redis.RedisClient

  val options = parseOption(args.toList)
  val r = new RedisClient("localhost", 6379)

  if (options.isDefinedAt('write)) {
    Console.err.println("putting data into database...")
    write()
  }
  if (options.isDefinedAt('search)) {
    Console.err.println( s"""searching the full name of "${ options('search) }"""")
    search(options('search))
  }

  def parseOption(args: List[String],
                  opts: Map[Symbol, String] = Map()): Map[Symbol, String] =
    args match {
      case "-write" :: rest => parseOption(rest, opts + ('write -> ""))
      case "-search" :: alias :: rest => parseOption(rest, opts + ('search -> alias))
      case Nil => opts
      case _ =>
        Console.err.println("Unknown option.")
        throw new NoSuchElementException
    }

  def search(alias: String) = r.getType(alias) match {
    // 配列なら、その先頭から末尾までを取得
    case Some("list") => r.lrange(alias, 0, -1).get.map(_.get) foreach println
    case t => Console.err.println(s"type mismatch: $t")
  }

  def write(): Unit = {
    r.flushall

    import org.json4s._
    import org.json4s.native.JsonMethods._

    Source.stdin.getLines().foreach { line =>
      val artist = parse(line)
      val name = (artist \ "name").values
      r.rpush(name, name)

      artist \ "aliases" match {
        case JArray(maps) => maps.foreach { map =>
          map \ "name" match {
            case JString(alias) => r.rpush(alias, name)
            case _ =>
          }
        }
        case _ =>
      }
    }
  }
}

/** アーティスト情報（artist.json.gz）をデータベースに登録せよ．
  * ただし，データベース名は"nlp100_{ユーザ名}"，コレクション名は"artist"とせよ．
  * さらに，次のフィールドでインデックスを作成せよ: name, aliases.name, tags.value, rating.value */
object P64 extends App {
  /** Preparation:
    * gzcat resources/artist.json.gz | mongoimport --host localhost --db nlp100_yuima --collection artist */

  import com.mongodb.casbah.Imports.{MongoDBObject => DBO, _}

  val mc = MongoClient("localhost", 27017)
  val db = mc("nlp100_yuima")
  val collection = db("artist")

  Seq("name", "aliases.name", "tags.value", "rating.value")
    .foreach(k => collection.createIndex(DBO(k -> 1)))
}

/** MongoDBのインタラクティブシェルを用いて，"Queen"というアーティストに関する情報を取得せよ．さらに，これと同様の処理を行うプログラムを実装せよ． */
object P65 extends App {
  /** % mongo nlp100_yuima
    * > db.artist.find({name: "Queen"}) */

  import com.mongodb.casbah.Imports.{MongoDBObject => DBO, _}

  val collection = MongoClient("localhost", 27017)("nlp100_yuima")("artist")

  collection.find(DBO("name" -> "Queen")) foreach println
}

/** MongoDBのインタラクティブシェルを用いて，活動場所が「Japan」となっているアーティスト数を求めよ． */
object P66 extends App {
  /** % mongo nlp100_yuima
    * > db.artist.count({"area":"Japan"}) */

  import com.mongodb.casbah.Imports.{MongoDBObject => DBO, _}

  val collection = MongoClient("localhost", 27017)("nlp100_yuima")("artist")

  //  println(collection.count($where("this.area == 'Japan'"))) // 遅い
  println(collection.count(DBO("area" -> "Japan")))
}

/** 特定の（指定した）別名を持つアーティストを検索せよ． */
object P67 extends App {

  import com.mongodb.casbah.Imports.{MongoDBObject => DBO, _}

  val collection = MongoClient("localhost", 27017)("nlp100_yuima")("artist")

  collection.find(DBO("aliases.name" -> args(0)))
    .map(_.get("name")) foreach println
}

/** "dance"というタグを付与されたアーティストの中でレーティングの投票数が多いアーティスト・トップ10を求めよ． */
object P68 extends App {

  import com.mongodb.casbah.Imports.{MongoDBObject => DBO, _}

  val collection = MongoClient("localhost", 27017)("nlp100_yuima")("artist")

  collection.find(DBO("tags.value" -> "dance"))
    .sort(DBO("rating.value" -> -1))
    .take(10)
    .map(_.get("name")) foreach println
}

object P69 extends App {
  println("Problem 69 is not revealed yet.")
}

