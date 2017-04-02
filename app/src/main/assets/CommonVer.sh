uniform mat4 uMVPMatrix; //外部带入
uniform mat4 uSTMatrix; //外部带入
attribute vec4 aPosition; //外部带入
attribute vec4 aTextureCoord; //外部带入
varying vec2 vTextureCoord; //送到FRA

void main()
{
    gl_Position = uMVPMatrix * aPosition;
    vTextureCoord = (uSTMatrix * aTextureCoord).xy;
}