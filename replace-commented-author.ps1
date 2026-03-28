$srcPath = "E:\blog\blog-springboot\src"
$count = 0
Get-ChildItem -Path $srcPath -Recurse -Filter "*.java" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw -Encoding UTF8
    if ($content -match "// \* @author ican") {
        $newContent = $content -replace "// \* @author ican", "// * @author Aric"
        [System.IO.File]::WriteAllText($_.FullName, $newContent, [System.Text.Encoding]::UTF8)
        $count++
        Write-Output "Updated: $($_.Name)"
    }
}
Write-Output "`nTotal files updated: $count"
