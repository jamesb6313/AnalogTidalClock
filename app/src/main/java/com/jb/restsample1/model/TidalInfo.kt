package com.jb.restsample1.model

import java.io.Serializable

class TidalInfo(
    val status: Int,
    val latitude: Float,
    val longitude: Float,
    val extremes: ArrayList<TidalExtremes>
) : Serializable

class TidalExtremes(
    val timestamp: Int,
    val datetime: String,
    val height: Float,
    val state: String
) : Serializable

/* - see file testJSON.json in asset folder
"extremes":[4 items
0:{4 items
"timestamp":1575921024
"datetime":"2019-12-09T19:50:24+00:00"
"height":-1.3568592864659292
"state":"LOW TIDE"
}
1:{4 items
"timestamp":1575943569
"datetime":"2019-12-10T02:06:09+00:00"
"height":1.3127446828570826
"state":"HIGH TIDE"
}
2:{4 items
"timestamp":1575965345
"datetime":"2019-12-10T08:09:05+00:00"
"height":-1.3735585028072412
"state":"LOW TIDE"
}
3:{4 items
"timestamp":1575987638
"datetime":"2019-12-10T14:20:38+00:00"
"height":1.378675961673515
"state":"HIGH TIDE"
}
]

 */