# spar-wings-spring-data-chunk

spring-data-mirage と組み合わせて、Mirage と Spring を連携します。

## SQL 発行

用途によって、interface を実装します。代表的な interface は以下の通りです。

* WritableRepository
	* INSERT / UPDATE / DELETE を発行する
* ReadableRepository
	* SELECT 文を発行する
	* `@Id` アノテーションを付与したカラムに対する SELECT 文を発行できます
* ChunkableRepository
	* Chunk 形式で結果を受け取る SELECT 文を発行する
	* ReadableRepository も含まれます
* SliceableRepository
	* Slice 形式で結果を受け取る SELECT 文を発行する
	* ReadableRepository も含まれます

用意する sql ファイルは spring-data-mirage を参照してください。

### Chunk とは？

ある集合に対する部分集合を表すリソースです。[参考](https://d1sraz2ju3uqe4.cloudfront.net/section2/example/resource/Chunk.html)
OFFSET を使用しないページネーションを提供します。後ろの部分集合を取得する際もレスポンスが落ちません。

前提条件として `@Id` を付与したカラムで大小比較を行い、ソートを行います。
その為、ORDER BY 句に `@Id` 以外のカラムを指定できません。

### Slice とは？

ある集合に対する部分集合を表すリソースです。
OFFSET を使用するページネーションを提供します。

後ろの部分集合を取得する際はレスポンスが悪化しますが ORDER BY 句に任意のカラムを指定可能です。

**集合の件数が少ない、または、先頭から数ページだけ参照することでユースケースが満たせる場合に限り**こちらを使用しても構いません。

Slice の時に発行する SELECT 文は以下のイメージです。

```sql
SELECT
	*
FROM
	some_table
WHERE
	-- デフォルトの絞り込み条件制御
	-- パラメータ有無で絞り込み条件制御
ORDER BY
	sort_column /*$direction*/ASC

/*BEGIN*/
LIMIT
	/*IF offset != null*/
	/*offset*/0,
	/*END*/

	/*IF size != null*/
	/*size*/10
	/*END*/
/*END*/
```

Repository の Sliceable パラメータで自動で設定するのは、

* `offset`
* `size`
* `direction`

です。

ソート順は、ユニークになるように指定してください。
例えば、`ORDER BY create_at ASC` にした場合、create_at が同じ値のレコードのソート順は不定の為、Sliceable で全ての値を取得することは保証できません。
以下のように
`ORDER BY create_at ASC, xxx_code ASC`
ソート条件にユニークキーを含めるようにしてください(xxx_code が当該 table のユニークキーの前提です)。

先頭から offset までの読み飛ばし件数が多くなることで API のパフォーマンス劣化を引き起こす可能性が高くなる為、
部分集合の先頭から 2000 件を超えて取得できないように制限します。
具体的には Sliceable パラメータの内容が `page_number * size + size > 2000` の場合、不正リクエストとみなし、InvalidSliceableException を throw します。

### INDEX 設計

MySQL の場合、
`ORDER BY create_at ASC, xxx_code ASC`
のソートに INDEX を効かせる為には、`create_at, xxx_code` の複合 INDEX が必要になります。`create_at` だけではソートに INDEX は効きません。
また、MySQL の場合、ソート条件に ASC と DESC が混在していると INDEX が効きません。設計時に留意してください。
(ただし、ソート対象のレコードが少ない場合は INDEX を貼っても使用されないので考慮する必要はありません)
絞り込み条件も存在する場合、適切な INDEX 設計を実施する必要があります。

## Controller で部分集合を取得するリクエストを受け取る

WebMvcConfigurer の実装クラスで addArgumentResolvers を Override し、

* ChunkableHandlerMethodArgumentResolver
	* Chunk のリクエストを受け取る場合
* SliceableHandlerMethodArgumentResolver
	* Slice のリクエストを受け取る場合

インスタンスを add してください。

イメージは以下の通りです。

```java
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new ChunkableHandlerMethodArgumentResolver());
		argumentResolvers.add(new SliceableHandlerMethodArgumentResolver());
	}
}
```

Controller の引数 Chunkable / Sliceable に `@ChunkableDefault` / `@SliceableDefault` を付与することで、
リクエストが未指定の時に defalut 値を設定したインスタンスを生成します。
