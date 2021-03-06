#ifndef HALF_PI
#define HALF_PI 1.5707963267948966
#endif

#define PROCESSING_TEXTURE_SHADER

float cross2( in vec2 a, in vec2 b ) {
	return a.x*b.y - a.y*b.x;
}

vec2 invBilinear( in vec2 p, in vec2 a, in vec2 b, in vec2 c, in vec2 d ){
    vec2 e = b-a;
    vec2 f = d-a;
    vec2 g = a-b+c-d;
    vec2 h = p-a;

    float k2 = cross2( g, f );
    float k1 = cross2( e, f ) + cross2( h, g );
    float k0 = cross2( h, e );

    float w = k1*k1 - 4.0*k0*k2;

    if( w<0.0 ) return vec2(-1.0);

    w = sqrt( w );

    float v1 = (-k1 - w)/(2.0*k2);
    float v2 = (-k1 + w)/(2.0*k2);
    float u1 = (h.x - f.x*v1)/(e.x + g.x*v1);
    float u2 = (h.x - f.x*v2)/(e.x + g.x*v2);
    bool  b1 = v1>0.0 && v1<1.0 && u1>0.0 && u1<1.0;
    bool  b2 = v2>0.0 && v2<1.0 && u2>0.0 && u2<1.0;

    vec2 res = vec2(-1.0);

    if(  b1 && !b2 ) res = vec2( u1, v1 );
    if( !b1 &&  b2 ) res = vec2( u2, v2 );

    return res;
}

float map(float value, float inMin, float inMax, float outMin, float outMax) {
  return outMin + (outMax - outMin) * (value - inMin) / (inMax - inMin);
}

float sineIn(float t) {
  return sin((t - 1.0) * HALF_PI) + 1.0;
}

uniform vec2 resolution;
uniform float rotateL;
uniform float rotateR;
uniform float uniformL;
uniform float uniformR;

uniform vec2 lu1;
uniform vec2 ru1;
uniform vec2 ro1;
uniform vec2 lo1;

uniform vec2 lu2;
uniform vec2 ru2;
uniform vec2 ro2;
uniform vec2 lo2;

uniform sampler2D texture;


varying vec4 vertTexCoord;

void main(void){

    vec2 pos1 = vec2(0.0001,0.0001); //lu
    vec2 pos2 = vec2(0.7999,0.0001); //ru
    vec2 pos3 = vec2(0.9999,0.9999);//ro
    vec2 pos4 = vec2(0.00001,0.9999);//lo

    //count
    float c = 2.;
    //aspect
    float aspect = resolution.x / resolution.y;
    aspect = vertTexCoord.x / vertTexCoord.y;

    float nonlinear = uniformL;
    float nonlinear2 = uniformR;

    vec2 uv = vertTexCoord.xy;
    vec2 uv2 = uv;

    float u_angle = rotateL;
    float sin_factor = sin(u_angle);
    float cos_factor = cos(u_angle);
    uv = vec2((uv.x -0.5) * aspect, uv.y - 0.5 ) * mat2(cos_factor, sin_factor, -sin_factor, cos_factor);

    float u_angle2 = rotateR;
    float sin_factor2 = sin(u_angle2);
    float cos_factor2 = cos(u_angle2);
    uv2 = vec2((uv2.x -0.5) * aspect, uv2.y - 0.5 ) * mat2(cos_factor2, sin_factor2, -sin_factor2, cos_factor2);

    uv.x += 1.;
    uv.y += 0.5;
    uv.x *= 0.5;

    vec2 texUv;
    vec2 texUv2;

    uv2.x *= 2.;
    uv.x *= 2.;

    //texUv = invBilinear(uv2,pos1,pos2,pos3,pos4);
    texUv = invBilinear(uv2,lu1,ru1,ro1,lo1);
    texUv.y = mix(texUv.y,sineIn(texUv.y),nonlinear);


    pos1.x += 1.;
    pos2.x += 1.;
    pos3.x += 1.;
    pos4.x += 1.;


    texUv2 = invBilinear(uv,pos1,pos2,pos3,pos4);
    //texUv2 = invBilinear(uv,lu2,ru2,ro2,lo2);
 	texUv2.y = mix(texUv2.y,sineIn(texUv2.y),nonlinear2);

    vec3 color;
    vec3 color2;

    if( texUv.x > -0.5 )
    {
        texUv.x *= 0.5;
        color = texture2D(texture, texUv).xyz;
    }

    if( texUv2.x > -0.5 )
    {

        texUv2.x *= 0.5;
        texUv2.x += 0.5;
        color2 = texture2D(texture, texUv2).xyz;
    }


    // Output to screen
    gl_FragColor = vec4(color,1.0);

    /*
    vec2 uv3 = vertTexCoord.xy;
    vec3 color3 = texture2D(texture, uv3).xyz;
    gl_FragColor = vec4(color3,1.);
    */

}
